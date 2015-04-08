package sample.distribute;

import akka.actor.UntypedActor;

public class Worker extends UntypedActor {

	@Override
	public void preStart() throws Exception {
	}

	@Override
	public void onReceive(Object arg0) throws Exception {
		System.out.println("Thread ID: " + Thread.currentThread().getId());
		if (arg0 instanceof Task) {
			System.out.println("Proccess: " + arg0 + " (" + self().path() + ")");
			sender().tell(new TaskDone((Task) arg0), getSelf());
			Thread.sleep(10000);
		}
	}

	@Override
	public void postStop() throws Exception {
	}

}
