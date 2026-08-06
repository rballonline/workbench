package com.tiltedev.springreactive.exception;

public class ApiNotFoundException extends ApiException {
    public ApiNotFoundException(String url, String message) {
        super(404, url, message);
    }
}
