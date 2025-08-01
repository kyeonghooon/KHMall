package com.khmall.exception.custom;

import com.khmall.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends BusinessException {

  public ForbiddenException(String message) {
    super(message, HttpStatus.FORBIDDEN);
  }

}
