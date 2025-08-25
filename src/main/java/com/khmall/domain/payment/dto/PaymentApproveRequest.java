package com.khmall.domain.payment.dto;

import com.khmall.common.constants.PaymentConstants;
import com.khmall.domain.payment.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentApproveRequest(
    @NotNull(message = PaymentConstants.PAYMENT_METHOD_NOT_NULL_MESSAGE)
    PaymentMethod paymentMethod,
    @NotNull(message = PaymentConstants.AMOUNT_NOT_BLANK_MESSAGE)
    @Positive(message = PaymentConstants.AMOUNT_POSITIVE_MESSAGE)
    Long amount
) {

}
