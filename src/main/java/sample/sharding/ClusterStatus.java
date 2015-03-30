package sample.sharding;

import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.Member;
import akka.cluster.ClusterEvent.ClusterDomainEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;

public class ClusterStatus extends UntypedActor {

	@Override
	public void preStart() throws Exception {
		Cluster.get(getContext().system()).subscribe(getSelf(), ClusterDomainEvent.class);
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof MemberUp) {
			Member m = ((MemberUp) message).member();
			System.out.println(m);
		} else if (message instanceof MemberRemoved) {
			Member m = ((MemberRemoved) message).member();
			System.out.println(m);
		} else {
			unhandled(message);
		}
	}

}
