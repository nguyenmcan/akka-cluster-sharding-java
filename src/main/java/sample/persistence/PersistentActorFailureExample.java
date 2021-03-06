package sample.persistence;

import java.util.ArrayList;

import scala.Option;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.Procedure;
import akka.persistence.UntypedPersistentActor;

public class PersistentActorFailureExample {
	public static class ExamplePersistentActor extends UntypedPersistentActor {
		private long sequenceNr = 0;

		@Override
		public String persistenceId() {
			return "sample-id-700";
		}

		private ArrayList<Object> received = new ArrayList<Object>();

		@Override
		public void preRestart(Throwable reason, Option<Object> message) {
		}
		
		@Override
		public void preStart() throws Exception {
			System.out.println(">>>>>>>>>>>>");
			super.preStart();
		}
		
		@Override
		public void onReceiveCommand(Object message) throws Exception {
			if (message.equals("boom")) {
				throw new Exception("boom");
			} else if (message.equals("print")) {
				System.out.println("received " + received);
			} else if (((String) message).startsWith("delete")) {
				deleteMessages(Long.parseLong(((String) message).split("-")[1]), true);
			} else if (message instanceof String) {
				String s = (String) message;
				persist(s, new Procedure<String>() {
					public void apply(String evt) throws Exception {
						received.add(evt);
						sequenceNr++;
					}
				});
			} else {
				unhandled(message);
			}
			System.out.println("sequenceNr: " + sequenceNr);
		}

		@Override
		public void onReceiveRecover(Object message) {
			System.out.println(">>>>>>>>>>>>>" + message);
			if (message instanceof String) {
				sequenceNr++;
				received.add((String) message);
				System.out.println("sequenceNr: " + sequenceNr);
			} else {
				unhandled(message);
			}
		}
	}

	public static void main(String... args) throws Exception {
		final ActorSystem system = ActorSystem.create("example");
		final ActorRef persistentActor = system.actorOf(Props.create(ExamplePersistentActor.class), "persistentActor-2");

		 persistentActor.tell("a", null);
		 persistentActor.tell("print", null);
		 persistentActor.tell("boom", null);
		 persistentActor.tell("print", null);
		 persistentActor.tell("b", null);
		 persistentActor.tell("print", null);
//		 persistentActor.tell("c", null);
//		 persistentActor.tell("print", null);

//		persistentActor.tell("delete-1", null);
//		persistentActor.tell("boom", null);
//		persistentActor.tell("delete-2", null);
//		persistentActor.tell("boom", null);
//		persistentActor.tell("delete-3", null);

		// Will print in a first run (i.e. with empty journal):

		// received [a]
		// received [a, b]
		// received [a, b, c]

		// Will print in a second run:

		// received [a, b, c, a]
		// received [a, b, c, a, b]
		// received [a, b, c, a, b, c]

		// etc ...

		Thread.sleep(1000);
		system.shutdown();
	}
}
