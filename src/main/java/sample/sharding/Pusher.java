package sample.sharding;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.contrib.pattern.ClusterSharding;

public class Pusher extends UntypedActor {

	private int counter;
	private String config;

	public Pusher() {
	}

	public Pusher(String config) {
		this.config = config;
	}

	@Override
	public void preStart() throws Exception {
		getContext().system().scheduler().schedule(Duration.create(0, TimeUnit.SECONDS), Duration.create(1, TimeUnit.SECONDS), new Runnable() {
			@Override
			public void run() {
				getSelf().tell(new Tick(), getSelf());
			}
		}, getContext().dispatcher());
	}

	@Override
	public void onReceive(Object arg0) throws Exception {
		if (arg0 instanceof Tick && "node2".equals(config)) {
			System.out.println(arg0);
			ActorRef counterRegion = ClusterSharding.get(getContext().system()).shardRegion("Counter");
			counterRegion.tell(new Counter.EntryEnvelope(counter, Counter.CounterOp.INCREMENT), getSelf());
			counter++;
		}
	}

	/**
	 * 
	 * @author can.nm
	 * 
	 */
	public static class Tick implements Serializable {
		private static final long serialVersionUID = 283770587844632431L;

	}

}
