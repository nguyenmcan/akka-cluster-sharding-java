package sample.sharding;

import java.io.Serializable;

public class EntryEnvelope implements Serializable {
	private static final long serialVersionUID = -6860107661346907344L;

	final public String id;
	final public Object payload;

	public EntryEnvelope(String id, Object payload) {
		this.id = id;
		this.payload = payload;
	}

	@Override
	public String toString() {
		return "{ EntryEnvelope:" + id + " }";
	}
}