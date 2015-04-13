package sample.distribute;

import java.io.Serializable;

import akka.routing.ConsistentHashingRouter.ConsistentHashable;

@SuppressWarnings("deprecation")
public class RetryTask implements Serializable, ConsistentHashable {
	private static final long serialVersionUID = 7782231832542116756L;

	public final Task task;

	public RetryTask(Task task) {
		this.task = task;
	}

	@Override
	public String toString() {
		return "[RetryTask:" + task + "]";
	}

	@Override
	public Object consistentHashKey() {
		return this.task.id;
	}

}
