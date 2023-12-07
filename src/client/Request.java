package client;

import java.io.Serializable;

public class Request implements Serializable{

	private static final long serialVersionUID = -5548531573238110706L;

	private String requestType;
	
	// Data in the request
	private String keyToSend;
	private String valueRetrieved;

	public String getRequestType() {
		return requestType;
	}
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	public String getKeyToSend() {
		return keyToSend;
	}
	public void setKeyToSend(String keyToSend) {
		this.keyToSend = keyToSend;
	}
	public String getValueRetrieved() {
		return valueRetrieved;
	}
	public void setValueRetrieved(String valueRetrieved) {
		this.valueRetrieved = valueRetrieved;
	}
	@Override
	public String toString() {
		return "Transaction [type=" + requestType + ", key=" + keyToSend + ", value=" + valueRetrieved
				+ "]";
	}
	
	
	
}
