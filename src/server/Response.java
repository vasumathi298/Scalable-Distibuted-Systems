package server;

import java.io.Serializable;

public class Response implements Serializable {

	private static final long serialVersionUID = 8949718079867932728L;
	
	// Type of the request to which server is responding
	private String responseType;
	
	// return value of the request
	private String returnValue;
	
	// Message describing what happened on the server side
	private String responseMessage;


	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}

	public String getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(String returnValue) {
		this.returnValue = returnValue;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	@Override
	public String toString() {
		return "Response [type=" + responseType + ", returnValue=" + returnValue
				+ ", message=" + responseMessage + "]";
	}
}
