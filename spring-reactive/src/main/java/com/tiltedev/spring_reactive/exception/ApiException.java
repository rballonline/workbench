package com.tiltedev.spring_reactive.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final int status;
    private final String url;

    public ApiException(int status, String url, String message) {
        super(message);
        this.status = status;
        this.url = url;
    }
}
