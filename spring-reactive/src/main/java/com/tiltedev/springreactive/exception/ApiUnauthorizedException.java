package com.tiltedev.springreactive.exception;

public class ApiUnauthorizedException extends ApiException {
    public ApiUnauthorizedException(String url, String message) {
        super(401, url, message);
    }
}
