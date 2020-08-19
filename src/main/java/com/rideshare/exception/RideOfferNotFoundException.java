package com.rideshare.exception;

public class RideOfferNotFoundException extends RuntimeException {
  public RideOfferNotFoundException(String msg, Throwable t) {
    super(msg, t);
  }

  public RideOfferNotFoundException(String msg) {
    super(msg);
  }

}
