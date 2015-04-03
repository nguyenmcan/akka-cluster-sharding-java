package sample.sharding;

import java.io.Serializable;

import akka.actor.UntypedActor;

public class Woker extends UntypedActor {

	public static class TaskDone implements Serializable {
		private static final long serialVersionUID = -5414439407575682893L;

		public final String id;

		public TaskDone(String id) {
			this.id = id;
		}
		
		@Override
		public String toString() {
			return "Done Task: " + id;
		}
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof Job) {
			System.out.println("Woker.process: " + msg);
			sender().tell(new TaskDone(((Job) msg).id), getSelf());
		}
	}

}
