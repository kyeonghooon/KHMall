package com.khmall.domain.order.dto;

import com.khmall.domain.order.OrderStatus;
import java.time.LocalDate;

public record OrderSearchCond(
    OrderStatus status,
    LocalDate from,
    LocalDate to,
    Long userId
) {

}
