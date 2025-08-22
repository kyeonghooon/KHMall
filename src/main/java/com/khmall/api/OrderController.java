package com.khmall.api;

import com.khmall.domain.order.OrderService;
import com.khmall.domain.order.OrderStatus;
import com.khmall.domain.order.dto.OrderCreateDirectRequest;
import com.khmall.domain.order.dto.OrderCreateFromCartRequest;
import com.khmall.domain.order.dto.OrderCreateResponse;
import com.khmall.domain.order.dto.OrderDetailResponse;
import com.khmall.domain.order.dto.OrderListView;
import com.khmall.domain.order.dto.OrderSearchCond;
import com.khmall.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrderController {

  private final OrderService orderService;

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/from-cart")
  public ResponseEntity<OrderCreateResponse> createFromCart(
      @AuthenticationPrincipal CustomUserDetails user,
      @Valid @RequestBody OrderCreateFromCartRequest request
  ) {
    Long userId = user.getUserId();
    OrderCreateResponse resp = orderService.createFromCart(userId, request);
    return ResponseEntity
        .created(URI.create("/orders/" + resp.orderId()))
        .body(resp);
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/direct")
  public ResponseEntity<OrderCreateResponse> createDirect(
      @AuthenticationPrincipal CustomUserDetails user,
      @Valid @RequestBody OrderCreateDirectRequest request
  ) {
    Long userId = user.getUserId();
    OrderCreateResponse resp = orderService.createDirect(userId, request);
    return ResponseEntity
        .created(URI.create("/orders/" + resp.orderId()))
        .body(resp);
  }

  @PostMapping("/{orderId}/done")
  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  public ResponseEntity<Void> complete(
      @PathVariable Long orderId,
      @AuthenticationPrincipal CustomUserDetails principal
  ) {
    boolean isAdmin = principal.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    orderService.complete(orderId, principal.getUserId(), isAdmin);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{orderId}/cancel")
  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  public ResponseEntity<Void> cancelBeforePay(
      @PathVariable Long orderId,
      @AuthenticationPrincipal CustomUserDetails principal
  ) {
    boolean isAdmin = principal.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    orderService.cancelBeforePay(orderId, principal.getUserId(), isAdmin);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{orderId}/refund")
  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  public ResponseEntity<Void> refundAfterPay(
      @PathVariable Long orderId,
      @AuthenticationPrincipal CustomUserDetails principal
  ) {
    boolean isAdmin = principal.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    orderService.refundAfterPay(orderId, principal.getUserId(), isAdmin);
    return ResponseEntity.ok().build();
  }

  // 회원: 내 주문 목록
  @GetMapping("/me")
  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  public ResponseEntity<Page<OrderListView>> myOrders(
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @PageableDefault Pageable pageable,
      @AuthenticationPrincipal CustomUserDetails principal
  ) {
    var cond = new OrderSearchCond(status, from, to, null);
    return ResponseEntity.ok(orderService.getMyOrders(principal.getUserId(), cond, pageable));
  }

  // 회원: 내 주문 상세
  @GetMapping("/orders/{orderId}")
  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  public ResponseEntity<OrderDetailResponse> myOrderDetail(
      @PathVariable Long orderId,
      @AuthenticationPrincipal CustomUserDetails principal
  ) {
    return ResponseEntity.ok(orderService.getMyOrderDetail(orderId, principal.getUserId()));
  }
}
