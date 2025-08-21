package com.khmall.domain.product.dto;

public record CustomerProductDetailResponse(
    Long id,
    String name,
    String description,
    Long price,
    String imageKey,
    String imageUrl,
    Integer quantity,
    boolean soldOut,
    Long categoryId,
    String categoryPath
) {

  public CustomerProductDetailResponse withImageUrl(String url) {
    return new CustomerProductDetailResponse(id, name, description, price, imageKey, url,
        quantity, soldOut, categoryId, categoryPath);
  }

  public CustomerProductDetailResponse withCategoryPath(String path) {
    return new CustomerProductDetailResponse(id, name, description, price, imageKey, imageUrl,
        quantity, soldOut, categoryId, path);
  }

  public CustomerProductDetailResponse withSoldOut(boolean so) {
    return new CustomerProductDetailResponse(id, name, description, price, imageKey, imageUrl,
        quantity, so, categoryId, categoryPath);
  }
}
