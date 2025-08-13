package com.khmall.domain.product.dto;

import com.khmall.domain.product.ProductStatus;
import org.openapitools.jackson.nullable.JsonNullable;

public record ProductUpdateRequest(
    JsonNullable<Long> categoryId,
    JsonNullable<String> name,
    JsonNullable<String> description,
    JsonNullable<String> imageKey,
    JsonNullable<Long> price,
    JsonNullable<ProductStatus> status,
    JsonNullable<Integer> quantity
) {

}
