package com.tn.maktba.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RevokedTokenException extends RuntimeException {
  public RevokedTokenException(String message) {
    super(message);
  }
}