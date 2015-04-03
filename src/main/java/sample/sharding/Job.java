package sample.sharding;

import java.io.Serializable;

public class Job implements Serializable {
	private static final long serialVersionUID = -5937641334320091219L;

	public final String id;
	public final int count;

	public Job(String id, int count) {
		this.id = id;
		this.count = count;
	}

	@Override
	public String toString() {
		return "Job-" + id + ": " + count;
	}
}
