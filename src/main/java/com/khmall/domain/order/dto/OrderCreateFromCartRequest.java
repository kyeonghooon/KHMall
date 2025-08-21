package com.khmall.domain.order.dto;

import com.khmall.common.constants.OrderConstants;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderCreateFromCartRequest(
    @NotEmpty(message = OrderConstants.CART_ITEM_NOT_EMPTY)
    List<Long> cartItemIds
) {

}
