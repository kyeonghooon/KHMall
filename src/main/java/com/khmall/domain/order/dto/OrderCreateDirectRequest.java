package com.khmall.domain.order.dto;

import com.khmall.common.constants.OrderConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderCreateDirectRequest(
    @NotNull(message = OrderConstants.PRODUCT_NOT_NULL)
    Long productId,

    @NotNull(message = OrderConstants.QUANTITY_NOT_NULL)
    @Min(value = 1, message = OrderConstants.QUANTITY_MIN)
    int quantity
) {

}
