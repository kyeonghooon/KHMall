package com.khmall.domain.cart.dto;

import com.khmall.common.constants.CartConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartAddRequest(
    @NotNull(message = CartConstants.PRODUCT_NOT_BLANK_MESSAGE)
    Long productId,
    @Min(value = 1, message = CartConstants.QUANTITY_INVALID_MESSAGE) int quantity
) {

}
