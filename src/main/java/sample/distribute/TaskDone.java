package sample.distribute;

import java.io.Serializable;

public class TaskDone implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1407763894561915627L;

	public final Task task;

	public TaskDone(Task task) {
		this.task = task;
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
