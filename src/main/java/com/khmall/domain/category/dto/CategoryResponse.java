package com.khmall.domain.category.dto;

public record CategoryResponse(
    Long categoryId,
    Long parentId,
    String name,
    Integer sortOrder
) {

}
