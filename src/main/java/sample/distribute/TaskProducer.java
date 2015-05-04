package sample.distribute;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;

public class TaskProducer extends AbstractActor {

	private Random rand = new Random();
	private String[] userIds = { "User-A", "User-B", "User-C" };
	private Map<String, AtomicInteger> counters = new HashMap<>();

	public TaskProducer() {
	}

	public static class Tick implements Serializable {
		private static final long serialVersionUID = 283770587844632431L;
	}

	@Override
	public void preStart() throws Exception {
		getContext().system().scheduler()
				.schedule(Duration.create(0, TimeUnit.SECONDS), Duration.create(2000, TimeUnit.MILLISECONDS), new Runnable() {
					@Override
					public void run() {
						getSelf().tell(new Tick(), getSelf());
					}
				}, getContext().dispatcher());
	}

	@Override
	public void onReceive(Object arg0) throws Exception {
		if (arg0 instanceof Tick) {
			String id = userIds[rand.nextInt(3)];
			AtomicInteger counter = counters.get(id);
			if (counter == null) {
				counter = new AtomicInteger();
				counters.put(id, counter);
			}
			ActorRef actorRef = getActorRef(TaskManager.class, "/user/producer-manager/producer/" + id, id, "dedicator-dispatcher");
			Task task = new Task(id, counter.incrementAndGet());
			actorRef.tell(task, getSelf());
			System.out.println(">>> Push: " + task);
		}
	}

	@Override
	public void postStop() throws Exception {
		super.postStop();
	}
}
