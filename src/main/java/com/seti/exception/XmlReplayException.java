package com.seti.exception;

public class XmlReplayException extends RuntimeException {

    public XmlReplayException(String message) {
        super(message);
    }

    public XmlReplayException(String message, Throwable cause) {
        super(message, cause);
    }
}