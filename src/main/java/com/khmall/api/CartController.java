package com.khmall.api;

import com.khmall.domain.cart.CartService;
import com.khmall.domain.cart.dto.CartAddRequest;
import com.khmall.domain.cart.dto.CartRemoveByIdsRequest;
import com.khmall.domain.cart.dto.CartResponse;
import com.khmall.domain.cart.dto.CartSetQuantityRequest;
import com.khmall.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/carts")
public class CartController {

  private final CartService cartService;

  @GetMapping
  public ResponseEntity<CartResponse> getCart(
      @AuthenticationPrincipal CustomUserDetails principal
  ) {
    return ResponseEntity.ok(cartService.get(principal.getUserId()));
  }

  @PostMapping("/items")
  public ResponseEntity<Void> addItem(
      @AuthenticationPrincipal CustomUserDetails principal,
      @Valid @RequestBody CartAddRequest request
  ) {
    cartService.add(principal.getUserId(), request);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/items/quantity")
  public ResponseEntity<Void> setQuantity(
      @AuthenticationPrincipal CustomUserDetails principal,
      @Valid @RequestBody CartSetQuantityRequest request
  ) {
    cartService.setQuantity(principal.getUserId(), request);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/items")
  public ResponseEntity<Void> removeItems(
      @AuthenticationPrincipal CustomUserDetails principal,
      @Valid @RequestBody CartRemoveByIdsRequest request
  ) {
    cartService.remove(principal.getUserId(), request);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping
  public ResponseEntity<Void> clearCart(
      @AuthenticationPrincipal CustomUserDetails principal
  ) {
    cartService.clear(principal.getUserId());
    return ResponseEntity.noContent().build();
  }

}
