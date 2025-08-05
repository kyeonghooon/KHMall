package com.khmall.domain.category.dto;

import static com.khmall.common.constants.CategoryConstants.NAME_MAX_LENGTH;
import static com.khmall.common.constants.CategoryConstants.NAME_NOT_BLANK_MESSAGE;
import static com.khmall.common.constants.CategoryConstants.NAME_SIZE_MESSAGE;
import static com.khmall.common.constants.CategoryConstants.SORT_ORDER_MIN;
import static com.khmall.common.constants.CategoryConstants.SORT_ORDER_MIN_MESSAGE;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
    Long parentId,

    @NotBlank(message = NAME_NOT_BLANK_MESSAGE)
    @Size(max = NAME_MAX_LENGTH, message = NAME_SIZE_MESSAGE)
    String name,

    @Min(value = SORT_ORDER_MIN, message = SORT_ORDER_MIN_MESSAGE)
    Integer sortOrder
) {

}
