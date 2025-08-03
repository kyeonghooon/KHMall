package com.khmall.exception.custom;

import com.khmall.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessException {

  public NotFoundException(String message) {
    super(message, HttpStatus.NOT_FOUND);
  }

}
