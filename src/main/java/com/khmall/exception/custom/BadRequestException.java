package com.khmall.exception.custom;

import com.khmall.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class BadRequestException extends BusinessException {

  public BadRequestException(String message) {
    super(message, HttpStatus.BAD_REQUEST);
  }
}
