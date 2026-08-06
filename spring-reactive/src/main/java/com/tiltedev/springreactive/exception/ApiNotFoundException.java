package com.tiltedev.spring_reactive.exception;

public class ApiNotFoundException extends ApiException {
    public ApiNotFoundException(String url, String message) {
        super(404, url, message);
    }
}
