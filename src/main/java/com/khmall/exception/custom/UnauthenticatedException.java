package com.khmall.exception.custom;

import com.khmall.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class UnauthenticatedException extends BusinessException {

  public UnauthenticatedException(String message) {
    super(message, HttpStatus.UNAUTHORIZED);
  }

}
