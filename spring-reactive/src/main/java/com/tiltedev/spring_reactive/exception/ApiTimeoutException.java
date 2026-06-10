package com.tiltedev.spring_reactive.exception;

public class ApiTimeoutException extends ApiException {
    public ApiTimeoutException(String url, String message) {
        super(504, url, message);
    }
}
