package sample.distribute;

import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.Duration;
import akka.actor.UntypedActor;

public class Worker extends UntypedActor {

	@Override
	public void preStart() throws Exception {
		context().setReceiveTimeout(Duration.create(120, TimeUnit.SECONDS));
	}

	@Override
	public void onReceive(Object arg0) throws Exception {
		if (arg0 instanceof Task) {
			System.out.println("Proccess: " + arg0 + " (" + self().path() + ")");
			Thread.sleep(5000);
			sender().tell(new TaskDone(), getSelf());
		}
	}

	@Override
	public void postStop() throws Exception {
	}

}
