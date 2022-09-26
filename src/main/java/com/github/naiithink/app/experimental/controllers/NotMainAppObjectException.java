package com.github.naiithink.app.experimental.controllers;

public final class NotMainAppObjectException
        extends Exception {

    public NotMainAppObjectException() {
    }

    public NotMainAppObjectException(String message) {
        super(message);
    }

    public NotMainAppObjectException(Throwable cause) {
        super(cause);
    }

    public NotMainAppObjectException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotMainAppObjectException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
