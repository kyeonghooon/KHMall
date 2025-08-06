package com.khmall.api;

import com.khmall.domain.category.CategoryService;
import com.khmall.domain.category.dto.CategoryCreateRequest;
import com.khmall.domain.category.dto.CategoryDeleteResult;
import com.khmall.domain.category.dto.CategoryResponse;
import com.khmall.domain.category.dto.CategoryUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
  public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
    return ResponseEntity.ok(categoryService.createCategory(request));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{categoryId}")
  public ResponseEntity<CategoryResponse> updateCategory(
      @PathVariable Long categoryId,
      @RequestBody CategoryUpdateRequest request) {
    return ResponseEntity.ok(categoryService.updateCategory(categoryId, request));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{categoryId}")
  public ResponseEntity<CategoryDeleteResult> deleteCategory(@PathVariable Long categoryId) {
    return ResponseEntity.ok(categoryService.deleteCategory(categoryId));
  }
}
