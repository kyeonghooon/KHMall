package com.khmall.domain.cart.dto;

public record CartItemResponse(
    Long cartItemId,
    Long productId,
    String name,
    String image,
    long unitPrice,
    int quantity,
    long lineTotal
) {

}
