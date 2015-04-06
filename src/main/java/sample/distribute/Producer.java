package sample.distribute;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;

public class Producer extends AbstractActor {

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
			String id = "6671d048-6326-47ed-9fd3-71e22d456e7d" /*UUID.randomUUID().toString()*/;
			ActorRef actorRef = getActorRef(Dedicator.class, id);
			actorRef.tell(new Task(id), getSelf());
			System.out.println(">>> Push: " + id);
		}
	}
}
