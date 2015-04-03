package sample.sharding;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.contrib.pattern.ClusterSharding;

public class Producer extends UntypedActor {

	private Random rand = new Random();
	private Map<String, AtomicInteger> counter = new HashMap<>();
	private static String[] uudis = { "user-1", "user-2", "user-3" };

	public Producer() {
	}

	public static class Tick implements Serializable {
		private static final long serialVersionUID = 283770587844632431L;
	}

	@Override
	public void preStart() throws Exception {
		getContext().system().scheduler()
				.schedule(Duration.create(0, TimeUnit.SECONDS), Duration.create(1000, TimeUnit.MILLISECONDS), new Runnable() {
					@Override
					public void run() {
						getSelf().tell(new Tick(), getSelf());
					}
				}, getContext().dispatcher());
	}

	@Override
	public void onReceive(Object arg0) throws Exception {
		if (arg0 instanceof Tick) {
			String id = uudis[rand.nextInt(uudis.length)];
			AtomicInteger count = counter.get(id);
			if (count == null) {
				count = new AtomicInteger(1);
				counter.put(id, count);
			}
			int delta = count.getAndIncrement();
			System.out.println("Push-" + id + ": " + delta);

			ActorRef counterRegion = ClusterSharding.get(getContext().system()).shardRegion(Main.ShardingName);
			counterRegion.tell(new EntryEnvelope(id, new Job(id, delta)), getSelf());
		}
	}

}
