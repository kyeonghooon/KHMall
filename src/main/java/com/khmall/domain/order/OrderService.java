package com.khmall.domain.order;

import com.khmall.common.constants.CartConstants;
import com.khmall.common.constants.InventoryConstants;
import com.khmall.common.constants.OrderConstants;
import com.khmall.common.constants.PaymentConstants;
import com.khmall.domain.cart.CartItem;
import com.khmall.domain.cart.CartItemRepository;
import com.khmall.domain.inventory.Inventory;
import com.khmall.domain.inventory.InventoryLog;
import com.khmall.domain.inventory.InventoryLogRepository;
import com.khmall.domain.inventory.InventoryMapper;
import com.khmall.domain.inventory.InventoryRepository;
import com.khmall.domain.order.dto.OrderCreateDirectRequest;
import com.khmall.domain.order.dto.OrderCreateFromCartRequest;
import com.khmall.domain.order.dto.OrderCreateResponse;
import com.khmall.domain.order.dto.OrderDetailResponse;
import com.khmall.domain.order.dto.OrderListView;
import com.khmall.domain.order.dto.OrderSearchCond;
import com.khmall.domain.payment.Payment;
import com.khmall.domain.payment.PaymentRepository;
import com.khmall.domain.payment.PaymentStatus;
import com.khmall.exception.custom.ConflictException;
import com.khmall.exception.custom.ForbiddenException;
import com.khmall.exception.custom.NotFoundException;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OrderService {

  private final CartItemRepository cartItemRepository;
  private final PaymentRepository paymentRepository;
  private final InventoryRepository inventoryRepository;
  private final InventoryLogRepository inventoryLogRepository;
  private final OrderRepository orderRepository;
  private final OrderQueryRepository orderQueryRepository;
  private final OrderCreateService orderCreateService;

  /**
   * 장바구니에서 주문 생성
   *
   * @param userId 사용자 ID
   * @param req    요청 정보
   * @return 주문 생성 응답
   */
  @Transactional
  public OrderCreateResponse createFromCart(Long userId, OrderCreateFromCartRequest req) {

    // 소유자 검증 + 잠금
    List<CartItem> cartItems =
        cartItemRepository.findAllByIdInAndCart_IdForUpdate(req.cartItemIds(), userId);
    if (cartItems.isEmpty()) {
      throw new NotFoundException(CartConstants.ITEM_NOT_FOUND);
    }

    List<OrderCreateService.Line> drafts = cartItems.stream()
        .map(ci -> new OrderCreateService.Line(ci.getProduct().getId(), ci.getQuantity()))
        .toList();

    OrderCreateResponse resp = orderCreateService.create(userId, drafts);

    // 성공 시 선택 항목 제거
    cartItemRepository.deleteAllInBatch(cartItems);
    return resp;
  }

  /**
   * 직접 주문 생성
   *
   * @param userId 사용자 ID
   * @param req    요청 정보
   * @return 주문 생성 응답
   */
  @Transactional
  public OrderCreateResponse createDirect(Long userId, OrderCreateDirectRequest req) {
    List<OrderCreateService.Line> drafts = List.of(new OrderCreateService.Line(req.productId(), req.quantity()));
    return orderCreateService.create(userId, drafts);
  }

  /**
   * 주문 배달 처리
   *
   * @param orderId 주문 ID
   */
  @Transactional
  public void deliver(Long orderId) {
    Order order = orderRepository.findByIdForUpdate(orderId)
        .orElseThrow(() -> new NotFoundException(OrderConstants.NOT_FOUND));

    if (order.getStatus() == OrderStatus.DELIVERY) {
      return;
    }

    if (order.getStatus() != OrderStatus.PAID) {
      throw new ConflictException(OrderConstants.NOT_PAID);
    }

    order.setStatus(OrderStatus.DELIVERY);
  }

  /**
   * 주문 완료 처리
   *
   * @param orderId 주문 ID
   * @param userId  사용자 ID
   * @param isAdmin 관리자 여부
   */
  @Transactional
  public void complete(Long orderId, Long userId, boolean isAdmin) {
    Order order = orderRepository.findByIdForUpdateWithUser(orderId)
        .orElseThrow(() -> new NotFoundException(OrderConstants.NOT_FOUND));

    Long ownerId = order.getUser().getId();
    if (!isAdmin && !Objects.equals(ownerId, userId)) {
      throw new ForbiddenException(OrderConstants.NOT_ORDER_OWNER);
    }

    if (order.getStatus() == OrderStatus.DONE) {
      return;
    }

    if (order.getStatus() != OrderStatus.DELIVERY) {
      throw new ConflictException(OrderConstants.NOT_DELIVERY);
    }

    order.setStatus(OrderStatus.DONE);
  }

  /**
   * 결제 완료 전 주문 취소
   *
   * @param orderId 주문 ID
   * @param userId  사용자 ID
   * @param isAdmin 관리자 여부
   */
  @Transactional
  public void cancelBeforePay(Long orderId, Long userId, boolean isAdmin) {
    Order order = loadAuthorize(orderId, userId, isAdmin);

    // 멱등
    if (order.getStatus() == OrderStatus.CANCEL) {
      return;
    }

    // 상태 검증: 결제 완료 전이어야 함
    if (order.getStatus() != OrderStatus.PAY_WAIT && order.getStatus() != OrderStatus.CREATED) {
      throw new ConflictException(OrderConstants.ALREADY_PAID);
    }

    // 활성 결제(READY) 취소 표시
    paymentRepository.findForUpdateByOrderIdAndStatusIn(order.getId(), List.of(PaymentStatus.READY))
        .ifPresent(p -> p.cancel(order.getUser()));

    // 재고 복원 + 로그
    finalizeCancel(order);
  }

  /**
   * 환불
   *
   * @param orderId 주문 ID
   * @param userId  사용자 ID
   * @param isAdmin 관리자 여부
   */
  @Transactional
  public void refundAfterPay(Long orderId, Long userId, boolean isAdmin) {

    Order order = loadAuthorize(orderId, userId, isAdmin);

    // 멱등
    if (order.getStatus() == OrderStatus.CANCEL) {
      return;
    }

    // 상태 검증 : 환불은 결제 완료 후에만 가능
    if (!EnumSet.of(OrderStatus.PAID, OrderStatus.DELIVERY, OrderStatus.DONE).contains(order.getStatus())) {
      throw new ConflictException(OrderConstants.NOT_PAID);
    }

    // 결제 환불 처리
    Payment paid = paymentRepository.findPaidForUpdateByOrderId(order.getId())
        .orElseThrow(() -> new ConflictException(PaymentConstants.PAID_NOT_FOUND));

    paid.cancel(order.getUser());

    // 재고 복원 + 로그
    finalizeCancel(order);
  }

  @Transactional(readOnly = true)
  public Page<OrderListView> getMyOrders(Long userId, OrderSearchCond cond, Pageable pageable) {
    OrderSearchCond effective = new OrderSearchCond(cond.status(), cond.from(), cond.to(), userId);
    return orderQueryRepository.search(effective, pageable);
  }

  @Transactional(readOnly = true)
  public OrderDetailResponse getMyOrderDetail(Long orderId, Long userId) {
    Order o = orderRepository.findDetailForUser(orderId, userId)
        .orElseThrow(() -> new NotFoundException(OrderConstants.NOT_FOUND));
    return OrderMapper.toDetailResponse(o);
  }

  @Transactional(readOnly = true)
  public Page<OrderListView> getAdminOrders(OrderSearchCond cond, Pageable pageable) {
    return orderQueryRepository.search(cond, pageable);
  }

  @Transactional(readOnly = true)
  public OrderDetailResponse getAdminOrderDetail(Long orderId) {
    Order o = orderRepository.findDetailForAdmin(orderId)
        .orElseThrow(() -> new NotFoundException(OrderConstants.NOT_FOUND));
    return OrderMapper.toDetailResponse(o);
  }

  private Order loadAuthorize(Long orderId, Long userId, boolean isAdmin) {
    Order order = orderRepository.findGraphForUpdate(orderId)
        .orElseThrow(() -> new NotFoundException(OrderConstants.NOT_FOUND));

    if (!isAdmin && !Objects.equals(order.getUser().getId(), userId)) {
      throw new ForbiddenException(OrderConstants.NOT_ORDER_OWNER);
    }
    return order;
  }

  private void finalizeCancel(Order order) {
    order.getItems().forEach(oi -> {
      // 재고 복원 (for update)
      Inventory inv = inventoryRepository.findByProductIdForUpdate(oi.getProduct().getId())
          .orElseThrow(() -> new NotFoundException(InventoryConstants.NOT_FOUND));
      inv.increase(oi.getQuantity());

      // 로그 기록
      InventoryLog log = InventoryMapper.toLogEntity(
          oi.getProduct(),
          oi.getQuantity(),
          InventoryConstants.REASON_RETURN
      );
      inventoryLogRepository.save(log);
    });

    // 주문 상태 CANCEL
    order.setStatus(OrderStatus.CANCEL);
  }
}
