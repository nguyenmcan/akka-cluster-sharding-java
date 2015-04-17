package sample.distribute;

import java.io.Serializable;

import akka.routing.ConsistentHashingRouter.ConsistentHashable;

@SuppressWarnings("deprecation")
public class Task implements Serializable, ConsistentHashable {
	private static final long serialVersionUID = 7782231832542116756L;

	private final String id;
	private final int count;
	private final long send_time;

	public Task(String id, int count) {
		this.id = id;
		this.count = count;
		this.send_time = System.currentTimeMillis();
	}

	@Override
	public String toString() {
		return "[Task:" + getId() + ":" + count + "]";
	}

	public boolean isTimeOut() {
		return System.currentTimeMillis() - send_time > 60 * 1000; // time-out 1
																	// minutes
	}

	@Override
	public Object consistentHashKey() {
		return getId();
	}

	public String getId() {
		return id;
	}

	public long getSend_time() {
		return send_time;
	}

}
