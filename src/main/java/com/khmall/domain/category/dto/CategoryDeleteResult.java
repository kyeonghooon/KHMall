package com.khmall.domain.category.dto;

import com.khmall.common.constants.CommonConstants;

public record CategoryDeleteResult(
    Long categoryId,
    String name,
    String message
) {
  public CategoryDeleteResult(Long categoryId, String name) {
    this(categoryId, name, CommonConstants.DELETE_SUCCESS);
  }
}
