package com.tiltedev.spring_reactive.exception;

public class ApiForbiddenException extends ApiException {
    public ApiForbiddenException(String url, String message) {
        super(403, url, message);
    }
}
