package sample.sharding;

import java.io.IOException;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.contrib.pattern.ClusterSharding;
import akka.contrib.pattern.ShardRegion;
import akka.persistence.journal.leveldb.SharedLeveldbStore;

import com.typesafe.config.ConfigFactory;

public class Main {

	public static final String ActorSystemName = "ShardingSystem";
	public static final String ShardingName = "ShardingActor";

	private static class Extractor implements ShardRegion.MessageExtractor {
		@Override
		public String entryId(Object message) {
//			System.out.println("1> " + message);
			if (message instanceof EntryEnvelope) {
				return String.valueOf(((EntryEnvelope) message).id);
			} else {
				return null;
			}
		}

		@Override
		public Object entryMessage(Object message) {
			if (message instanceof EntryEnvelope) {
				return ((EntryEnvelope) message).payload;
			} else {
				return message;
			}
		}

		@Override
		public String shardId(Object message) {
			int numberOfShards = 100;
			if (message instanceof EntryEnvelope) {
				long id = Math.abs(((EntryEnvelope) message).id.hashCode());
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
		ActorSystem system = ActorSystem.create(ActorSystemName, ConfigFactory.load("node2"));
		system.actorOf(Props.create(ClusterStatus.class));

		ClusterSharding.get(system).start(ShardingName, Props.create(Consumer.class), new Extractor());

//		if ("node1".equals(args[0])) {
			system.actorOf(Props.create(SharedLeveldbStore.class), "store");
//		}

		system.actorOf(Props.create(SharedStorageUsage.class));

//		if ("node2".equals(args[0])) {
			system.actorOf(Props.create(Producer.class));
//		}
		
	}
}
