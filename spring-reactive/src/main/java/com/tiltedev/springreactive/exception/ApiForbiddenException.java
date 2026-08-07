package com.tiltedev.springreactive.exception;

public class ApiForbiddenException extends ApiException {
  public ApiForbiddenException(String url, String message) {
    super(403, url, message);
  }
}
