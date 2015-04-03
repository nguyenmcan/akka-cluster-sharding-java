package sample.sharding;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.socket.Worker;

import sample.sharding.Woker.TaskDone;
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
	private EntryEnvelope currentTask;
	private Queue<EntryEnvelope> peddingTasks = new LinkedList<EntryEnvelope>();

	@Override
	public String persistenceId() {
		System.out.println(getSelf().path().name());
		return getSelf().path().parent().parent().name() + "-" + getSelf().path().name();
	}

	@Override
	public void preStart() throws Exception {
		super.preStart();
		workerRef = context().actorOf(Props.create(Worker.class));
		context().watch(workerRef);
		context().setReceiveTimeout(Duration.create(120, TimeUnit.SECONDS));
	}

	void updateTasks(EntryEnvelope task) {
		peddingTasks.offer(task);
	}

	@Override
	public void onReceiveRecover(Object msg) {
		if (msg instanceof EntryEnvelope) {
			updateTasks((EntryEnvelope) msg);
		} else {
			unhandled(msg);
		}
	}

	@Override
	public void onReceiveCommand(Object msg) {
		System.out.println("onReceiveCommand" + msg);
		if (msg instanceof EntryEnvelope) {
			persist((EntryEnvelope) msg, new Procedure<EntryEnvelope>() {
				public void apply(EntryEnvelope evt) {
					if (currentTask == null) {
						currentTask = evt;
						workerRef.tell(currentTask, getSelf());
					} else {
						updateTasks(evt);
					}
				}
			});
			System.out.println("Update Task List!");
		} else if (msg instanceof TaskDone) {
			currentTask = peddingTasks.poll();
			if (currentTask != null) {
				workerRef.tell(currentTask, getSelf());
			}
			System.out.println(msg);
		} else if (msg instanceof Terminated) {
			if (currentTask != null) {
				workerRef = context().actorOf(Props.create(Worker.class));
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
