package com.rideshare.exception;

public class ReservationExistException extends RuntimeException {
  public ReservationExistException(String msg, Throwable t) {
    super(msg, t);
  }

  public ReservationExistException(String msg) {
    super(msg);
  }

}
