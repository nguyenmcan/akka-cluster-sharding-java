package sample.distribute;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;

public class Producer extends AbstractActor {

	private AtomicInteger counter = new AtomicInteger();

	public Producer() {
	}

	public static class Tick implements Serializable {
		private static final long serialVersionUID = 283770587844632431L;
	}

	@Override
	public void preStart() throws Exception {
		getContext().system().scheduler()
				.schedule(Duration.create(0, TimeUnit.SECONDS), Duration.create(100, TimeUnit.MILLISECONDS), new Runnable() {
					@Override
					public void run() {
						getSelf().tell(new Tick(), getSelf());
					}
				}, getContext().dispatcher());
	}

	@Override
	public void onReceive(Object arg0) throws Exception {
		if (arg0 instanceof Tick) {
			String id = UUID.randomUUID().toString();
			ActorRef actorRef = getActorRef(Dedicator.class, "/user/" + id, id, "dedicator-dispatcher");
			Task task = new Task(id, counter.incrementAndGet());
			actorRef.tell(task, getSelf());
//			System.out.println(">>> Push: " + task);
		}
	}
}
