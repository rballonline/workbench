package com.tiltedev.spring_reactive.exception;

public class ApiBadRequestException extends ApiException {
    public ApiBadRequestException(String url, String message) {
        super(400, url, message);
    }
}
