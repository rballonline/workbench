package com.tiltedev.springreactive.exception;

public class ApiTimeoutException extends ApiException {
    public ApiTimeoutException(String url, String message) {
        super(504, url, message);
    }
}
