package com.rideshare.exception;

public class NoAvailableSeatException extends RuntimeException {
  public NoAvailableSeatException(String msg, Throwable t) {
    super(msg, t);
  }

  public NoAvailableSeatException(String msg) {
    super(msg);
  }

}
