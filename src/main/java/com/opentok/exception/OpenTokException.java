package com.opentok.exception;

public class OpenTokException extends Exception {
	private static final long serialVersionUID = 6059658348908505724L;

    String statusMessage;

    public OpenTokException(String statusMessage) {
        super();
        this.statusMessage = statusMessage;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
}