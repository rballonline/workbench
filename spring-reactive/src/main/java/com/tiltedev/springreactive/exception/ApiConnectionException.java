package com.tiltedev.springreactive.exception;

public class ApiConnectionException extends RuntimeException {

    private final String url;

    public ApiConnectionException(String url, String message, Throwable cause) {
        super(message, cause);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
