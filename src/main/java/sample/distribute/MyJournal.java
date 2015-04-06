package sample.distribute;

import scala.concurrent.Future;
import akka.japi.Procedure;
import akka.persistence.PersistentConfirmation;
import akka.persistence.PersistentId;
import akka.persistence.PersistentRepr;
import akka.persistence.journal.japi.SyncWriteJournal;

public class MyJournal extends SyncWriteJournal {

	@Override
	public void doDeleteMessages(Iterable<PersistentId> arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doDeleteMessagesTo(String arg0, long arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doWriteConfirmations(Iterable<PersistentConfirmation> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doWriteMessages(Iterable<PersistentRepr> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Future<Long> doAsyncReadHighestSequenceNr(String arg0, long arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<Void> doAsyncReplayMessages(String arg0, long arg1, long arg2, long arg3, Procedure<PersistentRepr> arg4) {
		// TODO Auto-generated method stub
		return null;
	}

}
