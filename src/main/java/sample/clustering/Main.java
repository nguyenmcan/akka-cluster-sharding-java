package sample.clustering;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.actor.Props;

public class Main {

	public static void main(String[] args) {
		ActorSystem system = ActorSystem.create("cluster-system", ConfigFactory.load(args[0]));
		if("node1".equals(args[0])) {
			system.actorOf(Props.create(Pusher.class));
		}
	}

}
