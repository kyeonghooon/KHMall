package com.khmall.domain.inventory;

import com.khmall.domain.product.Product;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryMapper {

  public static InventoryLog toLogEntity(Product product, int diffQty, String reason) {
    return InventoryLog.builder()
        .product(product)
        .diffQty(diffQty)
        .reason(reason)
        .build();
  }
}
