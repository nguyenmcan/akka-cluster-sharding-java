package sample.distribute;

import java.io.Serializable;

import akka.routing.ConsistentHashingRouter.ConsistentHashable;

@SuppressWarnings("deprecation")
public class Task implements Serializable, ConsistentHashable {
	private static final long serialVersionUID = 7782231832542116756L;

	public final String id;
	public final int count;

	public Task(String id, int count) {
		this.id = id;
		this.count = count;
	}

	@Override
	public String toString() {
		return "[Task:" + id + ":" + count + "]";
	}

	@Override
	public Object consistentHashKey() {
		return id;
	}

}
