package sample.distribute;

import java.io.Serializable;

import akka.routing.ConsistentHashingRouter.ConsistentHashable;

@SuppressWarnings("deprecation")
public class Task implements Serializable, ConsistentHashable {
	private static final long serialVersionUID = 7782231832542116756L;

	private String id;
	private int count;
	private long send_time;

	public Task(String id, int count) {
		this.setId(id);
		this.setCount(count);
		this.setSend_time(System.currentTimeMillis());
	}

	@Override
	public String toString() {
		return "[Task:" + getId() + ":" + getCount() + "]" + super.toString();
	}

	public void resetTimeOut() {
		this.setSend_time(System.currentTimeMillis());
	}

	public boolean isTimeOut() {
		// time-out after one minute
		return (System.currentTimeMillis() - getSend_time()) > (60 * 1000);
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

	public void setId(String id) {
		this.id = id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setSend_time(long send_time) {
		this.send_time = send_time;
	}

}
