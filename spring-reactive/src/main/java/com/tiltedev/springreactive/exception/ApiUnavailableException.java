package com.tiltedev.springreactive.exception;

public class ApiUnavailableException extends ApiException {
  public ApiUnavailableException(String url, String message) {
    super(503, url, message);
  }
}
