package sample.distribute;

import java.io.Serializable;

public class Task implements Serializable {
	private static final long serialVersionUID = 7782231832542116756L;

	public final String id;

	public Task(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "[Task:" + id + "]";
	}
	
}
