package com.github.naiithink.app.util.resources;

public class MalformedDataSourceException
        extends Exception {

    public MalformedDataSourceException() {
    }

    public MalformedDataSourceException(String message) {
        super(message);
    }

    public MalformedDataSourceException(Throwable cause) {
        super(cause);
    }

    public MalformedDataSourceException(String message,
                                        Throwable cause) {
        super(message, cause);
    }

    public MalformedDataSourceException(String message,
                                        Throwable cause,
                                        boolean enableSuppression,
                                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
