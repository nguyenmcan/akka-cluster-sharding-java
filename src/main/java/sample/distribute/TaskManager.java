package sample.distribute;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.actor.Terminated;
import akka.japi.Procedure;
import akka.persistence.SaveSnapshotSuccess;
import akka.persistence.UntypedPersistentActor;

public class TaskManager extends UntypedPersistentActor {

	private Task currentTask = null;
	private Queue<Task> taskList = new LinkedList<Task>();
	private ActorRef taskExecuter = null;

	@Override
	public void preStart() throws Exception {
		super.preStart();

		taskExecuter = context().actorOf(Props.create(TaskExecuter.class));
		context().watch(taskExecuter);

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

	@Override
	public void onReceiveCommand(Object arg0) throws Exception {
		if (arg0 instanceof Task) {
			System.out.println("Receive Task! " + self().path());
			persist((Task) arg0, new Procedure<Task>() {
				@Override
				public void apply(Task arg0) throws Exception {
					taskList.offer((Task) arg0);
					if (currentTask == null) {
						currentTask = taskList.poll();
						taskExecuter.tell(currentTask, getSelf());
					}
				}
			});
			if (taskList.size() % 10 == 0) {
				saveSnapshot(taskList);
			}

		} else if (arg0 instanceof TaskDone) {
			currentTask = taskList.poll();
			if (currentTask != null) {
				taskExecuter.tell(currentTask, getSelf());
			}
			System.out.println("Task Done! " + ((TaskDone) arg0).task);

		} else if (arg0 instanceof RetryTask) {
			taskExecuter.tell(((RetryTask) arg0).task, getSelf());
			System.out.println("[RetryTask] Retry send message: " + ((RetryTask) arg0).task);

		} else if (arg0 instanceof ReceiveTimeout) {
			self().tell(PoisonPill.getInstance(), getSelf());
			System.out.println("[ReceiveTimeout] Stop Actor!");

		} else if (arg0 instanceof Terminated) {
			if (taskExecuter.equals(((Terminated) arg0).getActor())) {
				taskExecuter = context().actorOf(Props.create(TaskExecuter.class));
				context().watch(taskExecuter);
				taskExecuter.tell(currentTask, getSelf());
			}

		} else if (arg0 instanceof SaveSnapshotSuccess) {
			deleteMessages(((SaveSnapshotSuccess) arg0).metadata().sequenceNr(), false);
		}
	}

	@Override
	public void postStop() {
		super.postStop();
	}

}
