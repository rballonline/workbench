package com.tiltedev.spring_reactive.exception;

public class ApiUnauthorizedException extends ApiException {
    public ApiUnauthorizedException(String url, String message) {
        super(401, url, message);
    }
}
