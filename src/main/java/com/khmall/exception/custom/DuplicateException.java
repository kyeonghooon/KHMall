package com.khmall.exception.custom;

import com.khmall.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class DuplicateException extends BusinessException {

  public DuplicateException(String message) {
    super(message, HttpStatus.CONFLICT);
  }

}
