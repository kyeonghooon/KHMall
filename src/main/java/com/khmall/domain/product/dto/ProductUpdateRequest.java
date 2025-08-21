package com.khmall.domain.product.dto;

import com.khmall.common.constants.ProductConstants;
import com.khmall.domain.product.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.openapitools.jackson.nullable.JsonNullable;

public record ProductUpdateRequest(
    JsonNullable<
        @NotNull(message = ProductConstants.CATEGORY_NOT_BLANK_MESSAGE) Long> categoryId,
    JsonNullable<
        @NotBlank(message = ProductConstants.NAME_NOT_BLANK_MESSAGE)
        @Size(max = ProductConstants.NAME_MAX_LENGTH, message = ProductConstants.NAME_SIZE_MESSAGE) String> name,
    JsonNullable<String> description,
    JsonNullable<
        @NotBlank(message = ProductConstants.IMAGE_KEY_NOT_BLANK_MESSAGE)
        @Pattern(
            regexp = ProductConstants.IMAGE_KEY_PATTERN,
            message = ProductConstants.IMAGE_KEY_PATTERN_MESSAGE
        ) String> imageKey,
    JsonNullable<
        @NotNull(message = ProductConstants.PRICE_NOT_BLANK_MESSAGE)
        @PositiveOrZero(message = ProductConstants.PRICE_MIN_MESSAGE) Long> price,
    JsonNullable<
        @NotNull(message = ProductConstants.STATUS_NOT_BLANK_MESSAGE) ProductStatus> status,
    JsonNullable<
        @NotNull(message = ProductConstants.QUANTITY_NOT_BLANK_MESSAGE)
        @PositiveOrZero(message = ProductConstants.QUANTITY_MIN_MESSAGE) Integer> quantity
) {

}
