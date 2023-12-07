package paxos;

import java.io.Serializable;

import client.Request;

public class PaxosPromise implements Serializable {
	
	private String serverID;
	
	private long sequenceNumber;
	
	private long previousSequenceNumber;
	 
	private Request previousTransactionValue;


	public String getServerID() {
		return serverID;
	}


	public long getPreviousSequenceNumber() {
		return previousSequenceNumber;
	}

	public void setServerID(String serverID) {
		this.serverID = serverID;
	}

	public void setPreviousSequenceNumber(long previousSequenceNumber) {
		this.previousSequenceNumber = previousSequenceNumber;
	}

	public long getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public Request getPreviousTransactionValue() {
		return previousTransactionValue;
	}

	public void setPreviousTransactionValue(Request previousTransactionValue) {
		this.previousTransactionValue = previousTransactionValue;
	}

}
