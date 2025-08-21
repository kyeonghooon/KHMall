package com.khmall.domain.cart.dto;

import com.khmall.common.constants.CartConstants;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CartRemoveByIdsRequest(
    @NotEmpty(message = CartConstants.PRODUCT_NOT_BLANK_MESSAGE)
    List<Long> cartItemIds
) {

}
