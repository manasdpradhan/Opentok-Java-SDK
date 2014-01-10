package com.opentok.exception;

public class OpentokRequestException extends OpenTokException {

    private static final long serialVersionUID = -3852834447530956514L;

    public OpentokRequestException(int code, String statusMessage) {
        super(code, statusMessage);

    }

}
