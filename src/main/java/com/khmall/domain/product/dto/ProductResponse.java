package com.khmall.domain.product.dto;

import com.khmall.domain.product.ProductStatus;
import java.time.LocalDateTime;

public record ProductResponse(
    Long id,
    Long categoryId,
    String name,
    String description,
    String imageKey,
    String imageUrl,
    Long price,
    ProductStatus status,
    Integer quantity,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}
