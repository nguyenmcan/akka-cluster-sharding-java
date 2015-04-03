package sample.sharding;

import java.io.Serializable;

import sample.sharding.Consumer.JobWrapper;
import akka.actor.UntypedActor;

public class Worker extends UntypedActor {

	public static class TaskDone implements Serializable {
		private static final long serialVersionUID = -5414439407575682893L;

		public final JobWrapper job;

		public TaskDone(JobWrapper job) {
			this.job = job;
		}
		
		@Override
		public String toString() {
			return "Done Task > " + job;
		}
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof JobWrapper) {
//			System.out.println("Woker Process: " + msg);
			sender().tell(new TaskDone(((JobWrapper) msg)), getSelf());
		}
	}

}
