package com.khmall.api;

import com.khmall.domain.order.OrderService;
import com.khmall.domain.order.dto.OrderCreateDirectRequest;
import com.khmall.domain.order.dto.OrderCreateFromCartRequest;
import com.khmall.domain.order.dto.OrderCreateResponse;
import com.khmall.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

}
