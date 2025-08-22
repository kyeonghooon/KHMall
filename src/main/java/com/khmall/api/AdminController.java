package com.khmall.api;

import com.khmall.domain.order.OrderService;
import com.khmall.domain.product.ProductService;
import com.khmall.domain.product.ProductStatus;
import com.khmall.domain.product.dto.AdminProductDetailResponse;
import com.khmall.domain.product.dto.AdminProductListView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

  private final ProductService productService;
  private final OrderService orderService;

  /**
   * 관리자용 상품 상세 조회 API
   *
   * @param id 상품 ID
   * @return 상품 상세 정보
   */
  @GetMapping("/products/{id}")
  public ResponseEntity<AdminProductDetailResponse> getDetail(@PathVariable Long id) {
    return ResponseEntity.ok(productService.getAdminProductDetail(id));
  }

  /**
   * 관리자용 상품 목록 조회 API
   *
   * @param keyword    검색어 (null이면 전체)
   * @param categoryId 카테고리 ID (null이면 전체)
   * @param status     상품 상태 (null이면 전체)
   * @param pageable   페이징 정보 (기본 20개, 생성일 기준 내림차순)
   * @return 상품 목록 페이지
   */
  @GetMapping("/products")
  public ResponseEntity<Page<AdminProductListView>> getList(
      @RequestParam(required = false) String keyword,
      @RequestParam(name = "category-id", required = false) Long categoryId,
      @RequestParam(required = false) ProductStatus status,
      @PageableDefault(size = 20, sort = "createdAt", direction = Direction.DESC)
      Pageable pageable
  ) {
    return ResponseEntity.ok(
        productService.getAdminProductList(keyword, categoryId, status, pageable)
    );
  }

  @PostMapping("/orders/{orderId}/deliver")
  public ResponseEntity<Void> deliverOrder(@PathVariable Long orderId) {
    orderService.deliver(orderId);
    return ResponseEntity.ok().build();
  }
}
