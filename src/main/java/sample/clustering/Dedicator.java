package sample.clustering;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import static akka.pattern.Patterns.ask;

public class Dedicator extends UntypedActor {

	private String id;
	private ActorRef workerRouter;
	private AtomicBoolean isProccessing;

	public Dedicator(String id, ActorRef workerRouter) {
		this.id = id;
		this.workerRouter = workerRouter;
	}
	
	@Override
	public void preStart() throws Exception {
		context().setReceiveTimeout(Duration.create(30, TimeUnit.SECONDS));
	}

	@Override
	public void onReceive(Object arg0) throws Exception {
		if ("Task".equals(arg0)) {
			Future<Object> future = ask(workerRouter, id, 10000);
			while(!future.isCompleted()) {
			}
			System.out.println(getSelf().path());
		} else if (arg0 instanceof Done) {
			isProccessing.set(false);
		}
	}

}
