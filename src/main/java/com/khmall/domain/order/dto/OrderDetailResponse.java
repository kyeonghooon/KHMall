package com.khmall.domain.order.dto;

import com.khmall.domain.order.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
    Long orderId,
    OrderStatus status,
    Long totalPrice,
    LocalDateTime createdAt,
    List<Item> items
) {

  public record Item(Long productId, String name, Long price, int quantity, String imageKey) {

  }
}
