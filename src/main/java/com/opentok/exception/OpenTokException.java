package com.opentok.exception;


public abstract class OpenTokException extends Exception {
	private static final long serialVersionUID = 6059658348908505724L;

	int errorCode;
    String statusMessage;

    public OpenTokException(int code, String statusMessage) {
        super();
        this.errorCode = code;
        this.statusMessage = statusMessage;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
}