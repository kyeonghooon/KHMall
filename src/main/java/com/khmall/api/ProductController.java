package com.khmall.api;

import com.khmall.domain.product.ProductService;
import com.khmall.domain.product.dto.CustomerProductDetailResponse;
import com.khmall.domain.product.dto.CustomerProductListView;
import com.khmall.domain.product.dto.ProductCreateRequest;
import com.khmall.domain.product.dto.ProductResponse;
import com.khmall.domain.product.dto.ProductUpdateRequest;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/products")
public class ProductController {

  private final ProductService productService;

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
    ProductResponse resp = productService.createProduct(request);
    URI location = URI.create("/api/products/" + resp.id());
    return ResponseEntity.created(location).body(resp);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{productId}")
  public ResponseEntity<ProductResponse> updateProduct(
      @PathVariable Long productId,
      @RequestBody ProductUpdateRequest request) {
    ProductResponse resp = productService.updateProduct(productId, request);
    URI location = URI.create("/api/products/" + resp.id());
    return ResponseEntity.ok().location(location).body(resp);
  }

  @GetMapping("/{productId}")
  public ResponseEntity<CustomerProductDetailResponse> getProductDetail(
      @PathVariable Long productId) {
    return ResponseEntity.ok(productService.getCustomerProductDetail(productId));
  }

  // 고객용 상품 목록 조회 API
  @GetMapping
  public ResponseEntity<Page<CustomerProductListView>> getProductList(
      @RequestParam(required = false) String keyword,
      @RequestParam(name = "category-id", required = false) Long categoryId,
      @PageableDefault(size = 20, sort = "createdAt", direction = Direction.DESC)
      Pageable pageable
  ) {
    return ResponseEntity.ok(productService.getCustomerProductList(keyword, categoryId, pageable));
  }
}
