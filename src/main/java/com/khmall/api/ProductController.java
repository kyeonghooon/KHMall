package com.khmall.api;

import com.khmall.domain.product.ProductService;
import com.khmall.domain.product.dto.ProductCreateRequest;
import com.khmall.domain.product.dto.ProductResponse;
import com.khmall.domain.product.dto.ProductUpdateRequest;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
