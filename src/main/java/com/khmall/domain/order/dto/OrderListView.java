package com.khmall.domain.order.dto;

import com.khmall.domain.order.OrderStatus;
import java.time.LocalDateTime;

public record OrderListView(
    Long orderId,
    OrderStatus status,
    Long totalPrice,
    LocalDateTime createdAt
) {

}
