package sample.clustering;

import akka.actor.UntypedActor;

public class Worker extends UntypedActor {

	@Override
	public void onReceive(Object arg0) throws Exception {
		if (arg0 instanceof String) {
			System.out.println(getSelf().path() + " > " + arg0);
		}
	}

}
