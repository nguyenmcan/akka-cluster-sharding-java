package sample.clustering;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.routing.FromConfig;

public class Pusher extends UntypedActor {

	private ActorRef workerRouter;
	private Random randNumber = new Random();
	private Map<String, ActorRef> actorRefs = new HashMap<>();
	private Map<ActorRef, String> idRefs = new HashMap<>();
	private Queue<ActorRef> freeActor = new ConcurrentLinkedQueue<>();

	@Override
	public void preStart() throws Exception {
		context().system().scheduler()
				.schedule(Duration.create(0, TimeUnit.MILLISECONDS), Duration.create(5000, TimeUnit.MILLISECONDS), new Runnable() {
					@Override
					public void run() {
						getSelf().tell("Tick", getSelf());
					}
				}, context().dispatcher());
		workerRouter = context().system().actorOf(FromConfig.getInstance().props(Props.create(Worker.class)), "router1");
	}

	@Override
	public void onReceive(Object arg0) throws Exception {
		if ("Tick".equals(arg0)) {
			String id = randNumber.nextInt(1000) + "";
			ActorRef ref;
			if (actorRefs.containsKey(id)) {
				ref = actorRefs.get(id);
			} else {
				ref = context().system().actorOf(Props.create(Dedicator.class, id, workerRouter), id);
				context().watch(ref);
				actorRefs.put(id, ref);
				idRefs.put(ref, id);
			}
			ref.tell("Task", getSelf());
		} else if (arg0 instanceof Done) {
			freeActor.offer(getSender());
		} else if (arg0 instanceof Terminated) {
			ActorRef actorRef = ((Terminated) arg0).actor();
			actorRefs.remove(idRefs.get(actorRef));
			idRefs.remove(actorRef);
		}
	}

}
