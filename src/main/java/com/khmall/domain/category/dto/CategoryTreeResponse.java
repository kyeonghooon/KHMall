package com.khmall.domain.category.dto;

import java.util.List;

public record CategoryTreeResponse(
    Long categoryId,
    String name,
    Integer sortOrder,
    List<CategoryTreeResponse> children
) {

}
