package com.khmall.domain.payment.dto;

import com.khmall.domain.order.OrderStatus;
import com.khmall.domain.payment.PaymentMethod;
import com.khmall.domain.payment.PaymentStatus;
import java.time.LocalDateTime;

public record PaymentApproveResponse(
    Long paymentId,
    PaymentStatus paymentStatus,
    Long orderId,
    OrderStatus orderStatus,
    PaymentMethod method,
    Long approvedAmount,
    LocalDateTime approvedAt
) {
}
