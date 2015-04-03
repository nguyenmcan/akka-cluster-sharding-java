package sample.sharding;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

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
	private JobWrapper currentTask;
	private AtomicLong taskCounter = new AtomicLong();
	private Queue<JobWrapper> peddingTasks = new LinkedList<JobWrapper>();

	public static class JobWrapper implements Serializable {
		private static final long serialVersionUID = -5117661658772564367L;

		public final long index;
		public final Job job;

		public JobWrapper(long index, Job job) {
			this.index = index;
			this.job = job;
		}

		@Override
		public String toString() {
			return index + " > Job Wrapper > " + job;
		}

	}

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

	void updateTasks(JobWrapper task) {
		peddingTasks.offer(task);
	}

	@Override
	public void onReceiveRecover(Object msg) {
		if (msg instanceof JobWrapper) {
			updateTasks((JobWrapper) msg);
			taskCounter.set(((JobWrapper) msg).index);
			System.out.println(">>>> Recover: " + ((JobWrapper) msg).job + " > " + ((JobWrapper) msg).index);
		} else {
			unhandled(msg);
		}
	}

	@Override
	public void onReceiveCommand(Object msg) {
		if (msg instanceof Job) {
			persist(new JobWrapper(taskCounter.incrementAndGet(), (Job) msg), new Procedure<JobWrapper>() {
				public void apply(JobWrapper evt) {
					updateTasks(evt);
					if (currentTask == null) {
						currentTask = peddingTasks.poll();
						workerRef.tell(currentTask, getSelf());
					}
				}
			});
			sender().tell(msg, sender);
//			System.out.println(String.format(">> Update task list for %s! total:%s", ((Job) msg).id, peddingTasks.size()));
		} else if (msg instanceof TaskDone) {
//			deleteMessages(((TaskDone) msg).job.index, false);
			currentTask = peddingTasks.poll();
			if (currentTask != null) {
				workerRef.tell(currentTask, getSelf());
			}
			System.out.println(msg);
		} else if (msg instanceof Terminated) {
			if (currentTask != null) {
				workerRef = context().actorOf(Props.create(Worker.class).withDispatcher("worker-dispatcher"));
				context().watch(workerRef);
				workerRef.tell(currentTask, getSelf());
			}
//			System.out.println("Worker Terminated!");
		} else if (msg.equals(ReceiveTimeout.getInstance())) {
			if (currentTask == null) {
				getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
			}
		} else {
			unhandled(msg);
		}
	}
}
