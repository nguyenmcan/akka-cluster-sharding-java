package sample.distribute;

import akka.actor.UntypedActor;

public class Worker extends UntypedActor {

	@Override
	public void preStart() throws Exception {
		super.preStart();
	}

	@Override
	public void onReceive(Object arg0) throws Exception {
		if (arg0 instanceof Task) {
			try {
				if(((Task) arg0).isTimeOut()) {
					// message timeout
					return;
				}
				System.out.println("Proccess: " + arg0 + " (" + self().path() + ")");
				sender().tell(new TaskDone((Task) arg0), getSelf());
			} catch (UnsupportedOperationException e) {
				sender().tell(new TaskDone((Task) arg0), getSelf());
			} catch (Exception e) {
				sender().tell(new RetryTask((Task) arg0), getSelf());
			}
		}
	}

	@Override
	public void postStop() throws Exception {
		super.postStop();
		System.out.println(">>>>>>>>>>>>>>>postStop!");
	}

}
