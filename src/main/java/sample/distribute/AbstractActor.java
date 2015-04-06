package sample.distribute;

import java.util.concurrent.TimeUnit;

import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.actor.UntypedActor;

public abstract class AbstractActor extends UntypedActor {

	protected ActorRef getActorRef(Class<?> clazz, String actorName) {
		try {
			ActorSelection actorSelection = context().system().actorSelection("/user/producer/" + actorName);
			return Await.result(actorSelection.resolveOne(Duration.create(10000, TimeUnit.MILLISECONDS)),
					Duration.create(10000, TimeUnit.MILLISECONDS));
		} catch (Exception e) {
			try {
				return context().actorOf(Props.create(Dedicator.class).withDispatcher("dedicator-dispatcher"), actorName);
			} catch (Exception e2) {
				System.err.println(e2.getMessage());
				return null;
			}
		}
	}
}
