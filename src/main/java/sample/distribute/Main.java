package sample.distribute;

import akka.actor.ActorSystem;
import akka.actor.Props;

import com.typesafe.config.ConfigFactory;

public class Main {

	public static final String ActorSystemName = "DistributeSystem";

	public static void main(String[] args) {
		ActorSystem system = ActorSystem.create(ActorSystemName, ConfigFactory.load("node1"));
		system.actorOf(Props.create(ClusterStatus.class));
		system.actorOf(Props.create(Producer.class), "producer");
	}

}
