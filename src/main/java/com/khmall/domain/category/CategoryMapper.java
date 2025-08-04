package com.khmall.domain.category;

import static com.khmall.common.constants.CategoryConstants.SORT_ORDER_MIN;

import com.khmall.domain.category.dto.CategoryCreateRequest;
import com.khmall.domain.category.dto.CategoryResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryMapper {

  public static CategoryResponse toResponse(Category category) {
    return new CategoryResponse(
        category.getCategoryId(),
        category.getParent() != null ? category.getParent().getCategoryId() : null,
        category.getName(),
        category.getSortOrder()
    );
  }

  public static Category toEntity(CategoryCreateRequest request, Category parent) {
    return Category.builder()
        .parent(parent)
        .name(request.name())
        .sortOrder(request.sortOrder() == null ? SORT_ORDER_MIN : request.sortOrder())
        .build();
  }

}
