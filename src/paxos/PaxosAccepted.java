package paxos;

import java.io.Serializable;

import client.Request;

public class PaxosAccepted implements Serializable {
	
	private String serverID;
	
	private long sequenceNumber;
	
	private Request transactionValue;

	public long findSequenceNumber() {
		return sequenceNumber;
	}

	public void storeSequenceNumber(long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public Request getTransactionValue() {
		return transactionValue;
	}

	public void setTransactionValue(Request transactionValue) {
		this.transactionValue = transactionValue;
	}
	public void setServerID(String serverID) {
		this.serverID = serverID;
	}

	public String getServerID() {
		return serverID;
	}

}
