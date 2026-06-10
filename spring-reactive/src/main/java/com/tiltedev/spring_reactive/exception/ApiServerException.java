package com.tiltedev.spring_reactive.exception;

public class ApiServerException extends ApiException {
    public ApiServerException(int status, String url, String message) {
        super(status, url, message);
    }
}
