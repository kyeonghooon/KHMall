package com.khmall.domain.product;

import com.khmall.domain.category.Category;
import com.khmall.domain.product.dto.ProductCreateRequest;
import com.khmall.domain.product.dto.ProductResponse;

public class ProductMapper {

  public static Product toEntity(ProductCreateRequest request, Category category) {
    return Product.builder()
        .category(category)
        .name(request.name())
        .description(request.description())
        .imageKey(request.imageKey())
        .price(request.price())
        .status(request.status())
        .build();
  }

  public static ProductResponse toResponse(Product product, int quantity, String imageUrl) {
    return new ProductResponse(
        product.getId(),
        product.getCategory().getCategoryId(),
        product.getName(),
        product.getDescription(),
        product.getImageKey(),
        imageUrl,
        product.getPrice(),
        product.getStatus(),
        quantity,
        product.getCreatedAt(),
        product.getUpdatedAt()
    );
  }
}
