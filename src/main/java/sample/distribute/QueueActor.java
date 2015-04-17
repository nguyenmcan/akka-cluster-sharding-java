package sample.distribute;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.DeadLetter;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.actor.Terminated;
import akka.japi.Procedure;
import akka.pattern.Patterns;
import akka.persistence.SaveSnapshotSuccess;
import akka.persistence.UntypedPersistentActor;
import akka.routing.CurrentRoutees;
import akka.routing.GetRoutees;

public class QueueActor extends UntypedPersistentActor {

	private Task currentTask = null;
	private Queue<Task> taskQueue = new LinkedList<Task>();
	private ActorRef router = null;
	private ActorRef masterRef = null;

	@Override
	public void preStart() throws Exception {
		super.preStart();

		router = getActorRef(Worker.class, "/user/router1", "router1", "router-dispatcher");

		context().system().eventStream().subscribe(self(), DeadLetter.class);

		context().setReceiveTimeout(Duration.create(120, TimeUnit.SECONDS));
	}

	@Override
	public String persistenceId() {
		return getSelf().path().parent().parent().name() + "-" + getSelf().path().name();
	}

	@Override
	public void onReceiveRecover(Object arg0) throws Exception {
		// if (arg0 instanceof Task) {
		// taskQueue.offer((Task) arg0);
		// } else if (arg0 instanceof SnapshotOffer) {
		// taskQueue = (Queue<Task>) ((SnapshotOffer) arg0).snapshot();
		// }
	}

	private ActorRef sendTask(Task task) {
		ActorRef actor = context().actorOf(Props.create(TaskManager.class, router));
		context().watch(actor);
		actor.tell(task, getSelf());
		return actor;
	}

	@Override
	public void onReceiveCommand(Object arg0) throws Exception {
		if (arg0 instanceof Task) {
			System.out.println("Receive Task! " + self().path());
			persist((Task) arg0, new Procedure<Task>() {
				@Override
				public void apply(Task arg0) throws Exception {
					taskQueue.offer((Task) arg0);
					if (currentTask == null) {
						currentTask = taskQueue.poll();
						sendTask(currentTask);
					}
				}
			});
			if (taskQueue.size() % 10 == 0) {
				saveSnapshot(taskQueue);
			}
			masterRef = getSender();
		} else if (arg0 instanceof TaskDone) {
			currentTask = taskQueue.poll();
			if (currentTask != null) {
				sendTask(currentTask);
			} else {
				// kill task manager
			}
			masterRef.tell(arg0, getSelf());
			System.out.println("Task Done! " + ((TaskDone) arg0).task);

		} else if (arg0 instanceof RetryTask) {
			sendTask(((RetryTask) arg0).task);
			System.out.println("[RetryTask] Retry send message: " + ((RetryTask) arg0).task);

		} else if (arg0 instanceof ReceiveTimeout) {
			self().tell(PoisonPill.getInstance(), getSelf());
			System.out.println("[ReceiveTimeout] Stop Actor!");

		} else if (arg0 instanceof Terminated) {
			((Terminated) arg0).getActor();

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
