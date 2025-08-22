package com.khmall.domain.payment;

import com.khmall.domain.order.Order;
import com.khmall.domain.payment.dto.PaymentApproveResponse;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class PaymentMapper {

  public static Payment toEntity(Order order) {
    return Payment.builder()
        .order(order)
        .amount(order.getTotalPrice())
        .build();
  }

  public static PaymentApproveResponse toPaymentApproveResponse(Payment payment, Order order) {
    return new PaymentApproveResponse(
        payment.getId(),
        payment.getStatus(),
        order.getId(),
        order.getStatus(),
        payment.getMethod(),
        payment.getAmount(),
        payment.getApprovedAt()
    );
  }
}
