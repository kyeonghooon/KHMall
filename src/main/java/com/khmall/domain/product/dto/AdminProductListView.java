package com.khmall.domain.product.dto;

import com.khmall.domain.product.ProductStatus;
import java.time.LocalDateTime;

public record AdminProductListView(
    Long id,
    String name,
    Long price,
    ProductStatus status,
    Long categoryId,
    String categoryName,
    String categoryPath,
    Integer quantity,
    LocalDateTime createdAt
) {
  public AdminProductListView withCategoryPath(String categoryPath) {
    return new AdminProductListView(
        id, name, price, status, categoryId, categoryName, categoryPath, quantity, createdAt);
  }
}
