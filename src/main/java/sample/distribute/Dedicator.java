package sample.distribute;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.DeadLetter;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.japi.Procedure;
import akka.persistence.SaveSnapshotSuccess;
import akka.persistence.UntypedPersistentActor;

public class Dedicator extends UntypedPersistentActor {

	private Task currentTask = null;
	private Queue<Task> taskQueue = new LinkedList<Task>();
	private ActorRef router = null;
	private ActorRef parentRef = null;

	@Override
	public void preStart() throws Exception {
		super.preStart();

		router = getActorRef(Worker.class, "/user/router1", "router1", "router-dispatcher");

		context().system().eventStream().subscribe(self(), DeadLetter.class);

		context().setReceiveTimeout(Duration.create(30, TimeUnit.SECONDS));
	}

	@Override
	public String persistenceId() {
		return getSelf().path().parent().parent().name() + "-" + getSelf().path().name();
	}

	@Override
	public void onReceiveRecover(Object arg0) throws Exception {
//		if (arg0 instanceof Task) {
//			taskQueue.offer((Task) arg0);
//		} else if (arg0 instanceof SnapshotOffer) {
//			taskQueue = (Queue<Task>) ((SnapshotOffer) arg0).snapshot();
//		}
	}

	@Override
	public void onReceiveCommand(Object arg0) throws Exception {
		if (arg0 instanceof Task) {
			System.out.println("Receive Task!");
			persist((Task) arg0, new Procedure<Task>() {
				@Override
				public void apply(Task arg0) throws Exception {
					taskQueue.offer((Task) arg0);
					if (currentTask == null) {
						currentTask = taskQueue.poll();
						router.tell(currentTask, getSelf());
					}
				}
			});
			if (taskQueue.size() % 10 == 0) {
				saveSnapshot(taskQueue);
			}
			parentRef = getSender();
		} else if (arg0 instanceof TaskDone) {
			currentTask = taskQueue.poll();
			if (currentTask != null) {
				router.tell(currentTask, getSelf());
			}
			parentRef.tell(arg0, getSelf());
			System.out.println("Task Done! " + ((TaskDone) arg0).task);
		} else if (arg0 instanceof ReceiveTimeout) {
			if (currentTask != null) {
				router.tell(currentTask, getSelf());
				System.out.println("[ReceiveTimeout] Retry send message: " + currentTask);
			} else {
				self().tell(PoisonPill.getInstance(), getSelf());
				System.out.println("[ReceiveTimeout] Stop Actor!");
			}
		} else if (arg0 instanceof DeadLetter) {
			if (((DeadLetter) arg0).message() == currentTask) {
				context().system().scheduler().scheduleOnce(Duration.create(1000, TimeUnit.MILLISECONDS), new Runnable() {
					@Override
					public void run() {
						router.tell(currentTask, getSelf());
						System.out.println("[DeadLetter] Retry send message: " + currentTask);
					}
				}, context().dispatcher());
			}
		} else if (arg0 instanceof SaveSnapshotSuccess) {
			deleteMessages(((SaveSnapshotSuccess) arg0).metadata().sequenceNr(), false);
		}
	}

	@Override
	public void postStop() {
		super.postStop();
	}

	/**
	 * 
	 */
	protected ActorRef getActorRef(Class<?> clazz, String actorPath, String actorName, String dispatcher) {
		try {
			ActorSelection actorSelection = context().system().actorSelection(actorPath);
			return Await.result(actorSelection.resolveOne(Duration.create(10000, TimeUnit.MILLISECONDS)),
					Duration.create(10000, TimeUnit.MILLISECONDS));
		} catch (Exception e) {
			try {
				return context().system().actorOf(Props.create(clazz).withDispatcher(dispatcher), actorName);
			} catch (Exception e2) {
				System.err.println(e2.getMessage());
				return null;
			}
		}
	}

}
