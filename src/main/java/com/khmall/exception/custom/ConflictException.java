package com.khmall.exception.custom;

import com.khmall.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ConflictException extends BusinessException {

  public ConflictException(String message) {
    super(message, HttpStatus.CONFLICT);
  }
}
