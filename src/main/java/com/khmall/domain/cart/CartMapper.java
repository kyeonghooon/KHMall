package com.khmall.domain.cart;

import com.khmall.domain.cart.dto.CartItemResponse;
import com.khmall.domain.cart.dto.CartResponse;
import com.khmall.domain.product.Product;
import com.khmall.domain.user.User;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CartMapper {

  public static Cart toEntity(User user) {
    return Cart.builder()
        .user(user)
        .build();
  }

  public static CartItem toItemEntity(Product product, int quantity) {
    return CartItem.builder()
        .product(product)
        .quantity(quantity)
        .build();
  }

  public static CartItemResponse toItemResponse(CartItem item, String imageUrl) {
    return new CartItemResponse(
        item.getId(),
        item.getProduct().getId(),
        item.getProduct().getName(),
        imageUrl,
        item.getProduct().getPrice(),
        item.getQuantity(),
        item.getProduct().getPrice() * item.getQuantity()
    );
  }

  public static CartResponse toResponse(Cart cart, List<CartItemResponse> items) {
    long totalPrice = items.stream()
        .mapToLong(CartItemResponse::lineTotal)
        .sum();

    int totalQuantity = items.stream()
        .mapToInt(CartItemResponse::quantity)
        .sum();

    return new CartResponse(
        cart.getId(),
        totalQuantity,
        totalPrice,
        cart.getUpdatedAt(),
        items
    );
  }
}
