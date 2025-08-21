package com.khmall.domain.product.dto;

import java.time.LocalDateTime;

public record CustomerProductListView(
    Long id,
    String name,
    Long price,
    String imageKey,
    String imageUrl,
    int quantity,
    boolean soldOut,
    Long categoryId,
    String categoryPath,
    LocalDateTime createdAt
) {

  public CustomerProductListView withImageUrl(String url) {
    return new CustomerProductListView(id, name, price, imageKey, url, quantity, soldOut, categoryId, categoryPath,
        createdAt);
  }

  public CustomerProductListView withCategoryPath(String path) {
    return new CustomerProductListView(id, name, price, imageKey, imageUrl, quantity, soldOut, categoryId, path, createdAt);
  }

  public CustomerProductListView withSoldOut(boolean so) {
    return new CustomerProductListView(id, name, price, imageKey, imageUrl, quantity, so, categoryId, categoryPath, createdAt);
  }
}
