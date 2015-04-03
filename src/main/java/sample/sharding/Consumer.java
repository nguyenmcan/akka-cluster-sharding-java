package sample.sharding;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import sample.sharding.Worker.TaskDone;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.actor.Terminated;
import akka.contrib.pattern.ShardRegion;
import akka.japi.Procedure;
import akka.persistence.UntypedPersistentActor;

public class Consumer extends UntypedPersistentActor {

	private ActorRef workerRef;
	private Job currentTask;
	private Queue<Job> peddingTasks = new LinkedList<Job>();

	@Override
	public String persistenceId() {
		return getSelf().path().parent().parent().name() + "-" + getSelf().path().name();
	}

	@Override
	public void preStart() throws Exception {
		super.preStart();
		workerRef = context().actorOf(Props.create(Worker.class).withDispatcher("worker-dispatcher"));
		context().watch(workerRef);
		context().setReceiveTimeout(Duration.create(120, TimeUnit.SECONDS));
	}

	void updateTasks(Job task) {
		peddingTasks.offer(task);
	}

	@Override
	public void onReceiveRecover(Object msg) {
		if (msg instanceof Job) {
			updateTasks((Job) msg);
		} else {
			unhandled(msg);
		}
	}

	@Override
	public void onReceiveCommand(Object msg) {
		if (msg instanceof Job) {
			persist((Job) msg, new Procedure<Job>() {
				public void apply(Job evt) {
					if (currentTask == null) {
						currentTask = evt;
						workerRef.tell(currentTask, getSelf());
					} else {
						updateTasks(evt);
					}
				}
			});
			System.out.println(String.format("Update task list for %s! total:%s", ((Job) msg).id, peddingTasks.size()));
		} else if (msg instanceof TaskDone) {
			currentTask = peddingTasks.poll();
			if (currentTask != null) {
				workerRef.tell(currentTask, getSelf());
			}
		} else if (msg instanceof Terminated) {
			if (currentTask != null) {
				workerRef = context().actorOf(Props.create(Worker.class).withDispatcher("worker-dispatcher"));
				context().watch(workerRef);
				workerRef.tell(currentTask, getSelf());
			}
			System.out.println("Worker Terminated!");
		} else if (msg.equals(ReceiveTimeout.getInstance())) {
			if (currentTask == null) {
				getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
			}
		} else {
			unhandled(msg);
		}
	}
}
