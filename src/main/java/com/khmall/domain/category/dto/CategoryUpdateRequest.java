package com.khmall.domain.category.dto;

import org.openapitools.jackson.nullable.JsonNullable;

public record CategoryUpdateRequest(
    JsonNullable<String> name,
    JsonNullable<Long> parentId,
    JsonNullable<Integer> sortOrder
) {

}
