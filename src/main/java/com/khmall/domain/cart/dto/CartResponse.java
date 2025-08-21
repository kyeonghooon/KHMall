package com.khmall.domain.cart.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CartResponse(
    Long cartId,
    int totalQuantity,
    long totalPrice,
    LocalDateTime updatedAt,
    List<CartItemResponse> items
) {

}
