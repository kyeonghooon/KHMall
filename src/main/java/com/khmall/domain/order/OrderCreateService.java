package com.khmall.domain.order;

import com.khmall.common.constants.InventoryConstants;
import com.khmall.common.constants.OrderConstants;
import com.khmall.common.constants.ProductConstants;
import com.khmall.domain.inventory.Inventory;
import com.khmall.domain.inventory.InventoryLog;
import com.khmall.domain.inventory.InventoryLogRepository;
import com.khmall.domain.inventory.InventoryRepository;
import com.khmall.domain.order.dto.OrderCreateResponse;
import com.khmall.domain.payment.Payment;
import com.khmall.domain.payment.PaymentMapper;
import com.khmall.domain.payment.PaymentRepository;
import com.khmall.domain.product.Product;
import com.khmall.domain.product.ProductRepository;
import com.khmall.domain.user.UserRepository;
import com.khmall.exception.custom.BadRequestException;
import com.khmall.exception.custom.NotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OrderCreateService {

  private final OrderRepository orderRepository;
  private final PaymentRepository paymentRepository;
  private final ProductRepository productRepository;
  private final InventoryRepository inventoryRepository;
  private final InventoryLogRepository inventoryLogRepository;
  private final UserRepository userRepository;

  @Transactional
  public OrderCreateResponse create(Long userId, List<Line> drafts) {
    if (drafts == null || drafts.isEmpty()) {
      throw new BadRequestException(OrderConstants.DRAFT_NOT_FOUND);
    }

    // 주문 본문 구성
    Order order = OrderMapper.initializeOrder(
        userRepository.getReferenceById(userId));

    Map<Product, Integer> decMap = new LinkedHashMap<>();

    for (Line d : drafts) {
      // 상품/재고 잠금
      Product p = productRepository.findByIdForUpdate(d.productId())
          .orElseThrow(() -> new NotFoundException(ProductConstants.NOT_FOUND));
      Inventory inv = inventoryRepository.findByProductIdForUpdate(p.getId())
          .orElseThrow(() -> new NotFoundException(InventoryConstants.NOT_FOUND));

      inv.decrease(d.quantity());

      // 가격 스냅샷 + 아이템 생성
      long priceSnapshot = p.getPrice();
      OrderItem oi = OrderItem.builder()
          .product(p)
          .price(priceSnapshot)
          .quantity(d.quantity())
          .build();

      order.addItem(oi);

      decMap.merge(p, d.quantity(), Integer::sum);
    }

    // 합계 확정
    order.recalcTotalPrice();
    order.setStatus(OrderStatus.PAY_WAIT);
    orderRepository.save(order);

    // 결제 생성
    Payment payment = PaymentMapper.toEntity(order);
    paymentRepository.save(payment);

    // 재고 로그 생성
    List<InventoryLog> logs = decMap.entrySet().stream()
        .map(en -> InventoryLog.builder()
            .product(en.getKey())
            .diffQty(-en.getValue())
            .reason(InventoryConstants.REASON_SELL)
            .build())
        .toList();
    inventoryLogRepository.saveAll(logs);

    return OrderMapper.toCreateResponse(order, payment);
  }

  public record Line(Long productId, int quantity) {}
}
