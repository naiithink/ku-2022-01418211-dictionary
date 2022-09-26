package com.github.naiithink.app.util.resources;

public class UnresolvableEscapeSequenceException
        extends Exception {

    public UnresolvableEscapeSequenceException() {
    }

    public UnresolvableEscapeSequenceException(String message) {
        super(message);
    }

    public UnresolvableEscapeSequenceException(Throwable cause) {
        super(cause);
    }

    public UnresolvableEscapeSequenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnresolvableEscapeSequenceException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
