package sample.sharding;

import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.Duration;
import akka.actor.PoisonPill;
import akka.actor.ReceiveTimeout;
import akka.contrib.pattern.ShardRegion;
import akka.japi.Procedure;
import akka.persistence.UntypedPersistentActor;

public class Counter extends UntypedPersistentActor {

	public static enum CounterOp {
		INCREMENT, DECREMENT
	}

	public static class Get {
		final public long counterId;

		public Get(long counterId) {
			this.counterId = counterId;
		}

		@Override
		public String toString() {
			return "Get: " + counterId;
		}
	}

	public static class EntryEnvelope {
		final public long id;
		final public Object payload;

		public EntryEnvelope(long id, Object payload) {
			this.id = id;
			this.payload = payload;
		}

		@Override
		public String toString() {
			return "EntryEnvelope: " + id;
		}
	}

	public static class CounterChanged {
		final public int delta;

		public CounterChanged(int delta) {
			this.delta = delta;
		}
	}

	int count = 0;

	@Override
	public String persistenceId() {
		System.out.println("======================>>>" + getSelf().path().parent().parent().name() + "-" + getSelf().path().name());
		return getSelf().path().parent().parent().name() + "-" + getSelf().path().name();
	}

	@Override
	public void preStart() throws Exception {
		super.preStart();
		context().setReceiveTimeout(Duration.create(120, TimeUnit.SECONDS));
	}

	void updateState(CounterChanged event) {
		count += event.delta;
	}

	@Override
	public void onReceiveRecover(Object msg) {
		System.out.println("======================>>>" + "Counter1: " + msg);
		if (msg instanceof CounterChanged)
			updateState((CounterChanged) msg);
		else
			unhandled(msg);
	}

	@Override
	public void onReceiveCommand(Object msg) {
		System.out.println("======================>>>" + "Counter2: " + msg);
		if (msg instanceof Get) {
			getSender().tell(count, getSelf());
		} else if (msg == CounterOp.INCREMENT) {
			persist(new CounterChanged(+1), new Procedure<CounterChanged>() {
				public void apply(CounterChanged evt) {
					updateState(evt);
				}
			});
		} else if (msg == CounterOp.DECREMENT) {
			persist(new CounterChanged(-1), new Procedure<CounterChanged>() {
				public void apply(CounterChanged evt) {
					updateState(evt);
				}
			});
		} else if (msg.equals(ReceiveTimeout.getInstance())) {
			getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
		} else {
			unhandled(msg);
		}
	}
}
