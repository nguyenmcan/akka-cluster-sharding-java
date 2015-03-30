package sample.sharding;

import java.io.IOException;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.contrib.pattern.ClusterSharding;
import akka.contrib.pattern.ShardRegion;

import com.typesafe.config.ConfigFactory;

public class Main {

	private static class Extractor implements ShardRegion.MessageExtractor {
		@Override
		public String entryId(Object message) {
			System.out.println("EntryId: " + message);
			if (message instanceof Counter.EntryEnvelope) {
				return String.valueOf(((Counter.EntryEnvelope) message).id);
			} else if (message instanceof Counter.Get) {
				return String.valueOf(((Counter.Get) message).counterId);
			} else {
				return null;
			}
		}

		@Override
		public Object entryMessage(Object message) {
			System.out.println("EntryMessage: " + message);
			if (message instanceof Counter.EntryEnvelope)
				return ((Counter.EntryEnvelope) message).payload;
			else
				return message;
		}

		@Override
		public String shardId(Object message) {
			System.out.println("ShardId: " + message);
			int numberOfShards = 2;
			if (message instanceof Counter.EntryEnvelope) {
				long id = ((Counter.EntryEnvelope) message).id;
				return String.valueOf(id % numberOfShards);
			} else if (message instanceof Counter.Get) {
				long id = ((Counter.Get) message).counterId;
				return String.valueOf(id % numberOfShards);
			} else {
				return null;
			}
		}
	}

	/**
	 * 
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		ActorSystem system = ActorSystem.create("cluster-system", ConfigFactory.load(args[0]));
		system.actorOf(Props.create(ClusterStatus.class));
		ClusterSharding.get(system).start("Counter", Props.create(Counter.class), new Extractor());
		system.actorOf(Props.create(Pusher.class, args[0]));
	}
}
