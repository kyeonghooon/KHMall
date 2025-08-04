package com.khmall.api;

import com.khmall.domain.category.CategoryService;
import com.khmall.domain.category.dto.CategoryCreateRequest;
import com.khmall.domain.category.dto.CategoryResponse;
import com.khmall.domain.category.dto.CategoryUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

  private final CategoryService categoryService;

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public CategoryResponse createCategory(@Valid @RequestBody CategoryCreateRequest request) {
    return categoryService.createCategory(request);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{categoryId}")
  public CategoryResponse updateCategory(
      @PathVariable Long categoryId,
      @RequestBody CategoryUpdateRequest request) {
    return categoryService.updateCategory(categoryId, request);
  }
}
