package com.khmall.domain.payment;

import com.khmall.common.constants.OrderConstants;
import com.khmall.common.constants.PaymentConstants;
import com.khmall.domain.order.Order;
import com.khmall.domain.order.OrderStatus;
import com.khmall.domain.payment.dto.PaymentApproveRequest;
import com.khmall.domain.payment.dto.PaymentApproveResponse;
import com.khmall.exception.custom.BadRequestException;
import com.khmall.exception.custom.ConflictException;
import com.khmall.exception.custom.ForbiddenException;
import com.khmall.exception.custom.NotFoundException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentService {

  private final PaymentRepository paymentRepository;

  @Transactional
  public PaymentApproveResponse approve(Long paymentId, PaymentApproveRequest req, Long userId, boolean isAdmin) {
    // 결제 + 주문 행 잠금
    Payment payment = paymentRepository.findByIdForUpdate(paymentId)
        .orElseThrow(() -> new NotFoundException(PaymentConstants.NOT_FOUND));

    Order order = payment.getOrder();

    // 소유자 검증
    Long ownerId = order.getUser().getId();
    if (!isAdmin && !Objects.equals(ownerId, userId)) {
      throw new ForbiddenException(OrderConstants.NOT_ORDER_OWNER);
    }

    // 이미 승인된 경우 그대로 응답
    if (payment.getStatus() == PaymentStatus.PAID) {
      return PaymentMapper.toPaymentApproveResponse(payment, order);
    }

    // 상태/금액 검증
    if (payment.getStatus() != PaymentStatus.READY) {
      throw new ConflictException(PaymentConstants.NOT_READY);
    }
    if (order.getStatus() != OrderStatus.PAY_WAIT) {
      throw new ConflictException(OrderConstants.NOT_PAY_WAIT);
    }
    long expected = order.getTotalPrice();
    if (!Objects.equals(expected, req.amount())) {
      throw new BadRequestException(PaymentConstants.AMOUNT_MISMATCH);
    }

    // 승인 처리
    payment.approve(req.paymentMethod());
    order.setStatus(OrderStatus.PAID);

    // 응답
    return PaymentMapper.toPaymentApproveResponse(payment, order);
  }
}
