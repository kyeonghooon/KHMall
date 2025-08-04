package com.khmall.domain.category.dto;

import static com.khmall.common.constants.CategoryConstants.NAME_MAX_LENGTH;
import static com.khmall.common.constants.CategoryConstants.SORT_ORDER_MIN;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
    Long parentId,

    @NotBlank(message = "카테고리명은 필수 입력입니다.")
    @Size(max = NAME_MAX_LENGTH, message = "카테고리명은 최대 " + NAME_MAX_LENGTH +"자까지 입력 가능합니다.")
    String name,

    @Min(value = SORT_ORDER_MIN, message = "정렬 순서는 " + SORT_ORDER_MIN + "이상이어야 합니다.")
    Integer sortOrder
) {

}
