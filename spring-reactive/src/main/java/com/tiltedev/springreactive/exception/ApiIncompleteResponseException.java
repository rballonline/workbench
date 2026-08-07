package com.tiltedev.springreactive.exception;

public class ApiIncompleteResponseException extends RuntimeException {

  private final String url;

  public ApiIncompleteResponseException(String url, String message, Throwable cause) {
    super(message, cause);
    this.url = url;
  }

  public String getUrl() {
    return url;
  }
}
