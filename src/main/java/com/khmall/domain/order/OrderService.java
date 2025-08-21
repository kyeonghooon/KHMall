package com.khmall.domain.order;

import com.khmall.common.constants.CartConstants;
import com.khmall.domain.cart.CartItem;
import com.khmall.domain.cart.CartItemRepository;
import com.khmall.domain.order.dto.OrderCreateDirectRequest;
import com.khmall.domain.order.dto.OrderCreateFromCartRequest;
import com.khmall.domain.order.dto.OrderCreateResponse;
import com.khmall.exception.custom.NotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OrderService {

  private final CartItemRepository cartItemRepository;
  private final OrderCreateService orderCreateService;

  @Transactional
  public OrderCreateResponse createFromCart(Long userId, OrderCreateFromCartRequest req) {

    // 소유자 검증 + 잠금
    List<CartItem> cartItems =
        cartItemRepository.findAllByIdInAndCart_IdForUpdate(req.cartItemIds(), userId);
    if (cartItems.isEmpty()) throw new NotFoundException(CartConstants.ITEM_NOT_FOUND);

    List<OrderCreateService.Line> drafts = cartItems.stream()
        .map(ci -> new OrderCreateService.Line(ci.getProduct().getId(), ci.getQuantity()))
        .toList();

    OrderCreateResponse resp = orderCreateService.create(userId, drafts);

    // 성공 시 선택 항목 제거
    cartItemRepository.deleteAllInBatch(cartItems);
    return resp;
  }

  @Transactional
  public OrderCreateResponse createDirect(Long userId, OrderCreateDirectRequest req) {
    List<OrderCreateService.Line> drafts = List.of(new OrderCreateService.Line(req.productId(), req.quantity()));
    return orderCreateService.create(userId, drafts);
  }

}
