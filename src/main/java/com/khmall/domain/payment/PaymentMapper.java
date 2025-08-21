package com.khmall.domain.payment;

import com.khmall.domain.order.Order;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class PaymentMapper {

  public static Payment toEntity(Order order) {
    return Payment.builder()
        .order(order)
        .amount(order.getTotalPrice())
        .build();
  }
}
