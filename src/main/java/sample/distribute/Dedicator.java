package sample.distribute;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.japi.Procedure;
import akka.persistence.SaveSnapshotFailure;
import akka.persistence.SaveSnapshotSuccess;
import akka.persistence.SnapshotOffer;
import akka.persistence.UntypedPersistentActor;

public class Dedicator extends UntypedPersistentActor {

	private Task currentTask = null;
	private Queue<Task> taskQueue = new LinkedList<Task>();
	private ActorRef actorRef = null;

	@Override
	public String persistenceId() {
		return getSelf().path().parent().parent().name() + "-" + getSelf().path().name();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onReceiveRecover(Object arg0) throws Exception {
		System.out.println("Recover: " + arg0);
		if (arg0 instanceof Task) {
			taskQueue.offer((Task) arg0);
		} else if (arg0 instanceof SnapshotOffer) {
			taskQueue = (Queue<Task>) ((SnapshotOffer) arg0).snapshot();
		}
	}

	@Override
	public void onReceiveCommand(Object arg0) throws Exception {
		if (arg0 instanceof Task) {
			persist((Task) arg0, new Procedure<Task>() {
				@Override
				public void apply(Task arg0) throws Exception {
					taskQueue.offer((Task) arg0);
					System.out.println("Queue Size: " + taskQueue.size());
					if (currentTask == null) {
						currentTask = taskQueue.poll();
						actorRef = context().actorOf(Props.create(Worker.class).withDispatcher("worker-dispatcher"));
						context().watch(actorRef);
						actorRef.tell(currentTask, getSelf());
					}
				}
			});
			if (taskQueue.size() % 10 == 0) {
				saveSnapshot(taskQueue);
			}
		} else if (arg0 instanceof TaskDone) {
			currentTask = taskQueue.poll();
			if (currentTask != null) {
				sender().tell(currentTask, getSelf());
			} else {
				sender().tell(PoisonPill.getInstance(), self());
			}
			System.out.println("Task Done!");
		} else if (arg0 instanceof Terminated) {
			ActorRef actorRef2 = ((Terminated) arg0).actor();
			if (actorRef2 == actorRef && currentTask != null) {
				actorRef = context().actorOf(Props.create(Worker.class).withDispatcher("worker-dispatcher"));
				context().watch(actorRef);
				actorRef.tell(currentTask, getSelf());
			}
		} else if (arg0 instanceof SaveSnapshotSuccess) {
			deleteMessages(((SaveSnapshotSuccess) arg0).metadata().sequenceNr(), false);
			System.out.println("SaveSnapshotSuccess");
		} else if (arg0 instanceof SaveSnapshotFailure) {
			System.out.println("SaveSnapshotFailure");
		} else {
			System.out.println("Deditator Unhandle: " + arg0);
		}
	}

	/**
	 * 
	 */
	protected ActorRef getActorRef(Class<?> clazz, String actorName) {
		try {
			ActorSelection actorSelection = context().system().actorSelection("/user/producer/" + actorName);
			return Await.result(actorSelection.resolveOne(Duration.create(10000, TimeUnit.MILLISECONDS)),
					Duration.create(10000, TimeUnit.MILLISECONDS));
		} catch (Exception e) {
			try {
				return context().actorOf(Props.create(Dedicator.class).withDispatcher("dedicator-dispatcher"), actorName);
			} catch (Exception e2) {
				System.err.println(e2.getMessage());
				return null;
			}
		}
	}

}
