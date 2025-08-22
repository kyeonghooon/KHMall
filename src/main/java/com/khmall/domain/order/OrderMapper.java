package com.khmall.domain.order;

import com.khmall.domain.order.dto.OrderCreateResponse;
import com.khmall.domain.order.dto.OrderDetailResponse;
import com.khmall.domain.payment.Payment;
import com.khmall.domain.user.User;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class OrderMapper {

  public static Order initializeOrder(User user) {
    return Order.builder()
        .user(user)
        .status(OrderStatus.CREATED)
        .build();
  }

  public static OrderCreateResponse toCreateResponse(Order order, Payment payment) {
    return new OrderCreateResponse(
        order.getId(),
        payment.getId(),
        order.getTotalPrice(),
        order.getStatus().name(),
        payment.getStatus().name(),
        toItemDtos(order.getItems())
    );
  }

  public static List<OrderCreateResponse.Item> toItemDtos(List<OrderItem> orderItems) {
    return orderItems.stream()
        .map(OrderMapper::toItemDto)
        .toList();
  }

  public static OrderCreateResponse.Item toItemDto(OrderItem oi) {
    long lineTotal = oi.getPrice() * oi.getQuantity();
    Long productId = (oi.getProduct() != null) ? oi.getProduct().getId() : null;
    return new OrderCreateResponse.Item(
        productId,
        oi.getPrice(),
        oi.getQuantity(),
        lineTotal
    );
  }

  public static OrderDetailResponse toDetailResponse(Order order) {
    List<OrderDetailResponse.Item> items = order.getItems().stream()
        .map(i -> new OrderDetailResponse.Item(
            i.getProduct().getId(),
            i.getProduct().getName(),
            i.getPrice(),
            i.getQuantity(),
            i.getProduct().getImageKey()
        )).toList();

    return new OrderDetailResponse(
        order.getId(), order.getStatus(), order.getTotalPrice(), order.getCreatedAt(), items
    );
  }
}
