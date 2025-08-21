package com.khmall.domain.product.dto;

import com.khmall.domain.product.ProductStatus;
import java.time.LocalDateTime;

public record AdminProductDetailResponse(
    Long id,
    String name,
    String description,
    String imageKey,
    String imageUrl,
    Long price,
    ProductStatus status,
    Long categoryId,
    String categoryName,
    String categoryPath,
    Integer quantity,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long createdBy,
    Long updatedBy
) {
  public AdminProductDetailResponse withCategoryPathAndImageUrl(
      String categoryPath, String imageUrl) {
    return new AdminProductDetailResponse(
        id,
        name,
        description,
        imageKey,
        imageUrl,
        price,
        status,
        categoryId,
        categoryName,
        categoryPath,
        quantity,
        createdAt,
        updatedAt,
        createdBy,
        updatedBy);
  }

}
