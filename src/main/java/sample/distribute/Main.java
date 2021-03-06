package sample.distribute;

import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.contrib.pattern.ClusterSingletonManager;
import akka.routing.FromConfig;

import com.typesafe.config.ConfigFactory;

public class Main {

	public static final String ActorSystemName = "ActorSystem";

	public static void main(String[] args) {
		ActorSystem system = ActorSystem.create(ActorSystemName, ConfigFactory.load(args[0]));
		system.actorOf(Props.create(ClusterStatus.class));

		system.actorOf(FromConfig.getInstance().props(Props.create(Worker.class)).withDispatcher("router-dispatcher"), "router1");

		system.actorOf(
				ClusterSingletonManager.defaultProps(Props.create(TaskProducer.class).withDispatcher("producer-dispatcher"), "producer",
						PoisonPill.getInstance(), ""), "producer-manager");
	}

}
