package com.khmall.domain.inventory;

import com.khmall.common.constants.InventoryConstants;
import com.khmall.domain.product.Product;
import com.khmall.exception.custom.BadRequestException;
import com.khmall.exception.custom.ConflictException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "inventory")
public class Inventory {

  @Id
  @Column(name = "product_id")
  private Long productId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  public Inventory(Product product, int quantity) {
    validateNonNegative(quantity);
    this.product = product;
    this.quantity = quantity;
  }

  public void increase(int qty) {
    validatePositive(qty);
    this.quantity += qty;
  }

  public void decrease(int qty) {
    validatePositive(qty);
    if (this.quantity < qty) {
      throw new ConflictException(
          String.format(InventoryConstants.INSUFFICIENT_STOCK_MESSAGE, qty, this.quantity));
    }
    this.quantity -= qty;
  }

  public void overwrite(int newQty) {
    validateNonNegative(newQty);
    this.quantity = newQty;
  }

  public boolean canDecrease(int qty) {
    return qty > 0 && this.quantity >= qty;
  }

  private static void validatePositive(int q) {
    if (q <= 0) throw new BadRequestException(InventoryConstants.QUANTITY_POSITIVE_MESSAGE);
  }

  private static void validateNonNegative(int q) {
    if (q < 0) throw new BadRequestException(InventoryConstants.QUANTITY_NON_NEGATIVE_MESSAGE);
  }
}
