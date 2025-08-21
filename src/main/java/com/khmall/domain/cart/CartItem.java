package com.khmall.domain.cart;

import com.khmall.domain.product.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Entity
@Table(
    name = "cart_item",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_cart_item_cart_product", columnNames = {"cart_id", "product_id"})
    },
    indexes = {
        @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
        @Index(name = "idx_cart_item_product", columnList = "product_id")
    }
)
public class CartItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "cart_item_id")
  private Long id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cart_id", nullable = false)
  private Cart cart;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Min(1)
  @Column(name = "quantity", nullable = false)
  private int quantity;

  // Cart 양방향 연결 전용
  void setCart(Cart cart) {
    this.cart = cart;
  }

  // 수량 변경
  void setQuantity(int quantity) {
    this.quantity = quantity;
    if (this.cart != null) {
      this.cart.markAsUpdated();
    }
  }
}
