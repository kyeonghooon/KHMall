package com.khmall.domain.product;

import com.khmall.common.constants.ProductConstants;
import com.khmall.exception.custom.BadRequestException;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ProductValidator {

  public static void validateProductName(String name) {
    if (name == null || name.isBlank()) {
      throw new BadRequestException(ProductConstants.NAME_NOT_BLANK_MESSAGE);
    }
    if (name.length() > ProductConstants.NAME_MAX_LENGTH) {
      throw new BadRequestException(ProductConstants.NAME_SIZE_MESSAGE);
    }
  }

}
