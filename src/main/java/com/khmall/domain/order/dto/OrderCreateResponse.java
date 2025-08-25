package com.khmall.domain.order.dto;

import java.util.List;

public record OrderCreateResponse(
    Long orderId,
    Long paymentId,
    long amount,
    String orderStatus,
    String paymentStatus,
    List<Item> items
) {
  public record Item(Long productId, long price, int quantity, long lineTotal) {}
}
