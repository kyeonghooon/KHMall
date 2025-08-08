package com.khmall.domain.product;

import static org.assertj.core.api.Assertions.assertThat;

import com.khmall.common.constants.InventoryConstants;
import com.khmall.domain.category.Category;
import com.khmall.domain.category.CategoryRepository;
import com.khmall.domain.inventory.Inventory;
import com.khmall.domain.inventory.InventoryLog;
import com.khmall.domain.inventory.InventoryLogRepository;
import com.khmall.domain.inventory.InventoryRepository;
import com.khmall.domain.product.dto.ProductCreateRequest;
import com.khmall.domain.product.dto.ProductResponse;
import com.khmall.support.AuthenticatedServiceTestBase;
import com.khmall.util.TestEntityFactory;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProductCreateTest extends AuthenticatedServiceTestBase {

  @Autowired
  ProductService productService;
  @Autowired
  CategoryRepository categoryRepository;
  @Autowired
  InventoryRepository inventoryRepository;
  @Autowired
  InventoryLogRepository inventoryLogRepository;
  @Autowired
  ProductRepository productRepository;

  @Test
  @DisplayName("상품 생성 테스트")
  void 상품등록_성공() {
    // Given
    Category category = TestEntityFactory.createCategory(categoryRepository, null, "테스트카테고리", 0);

    ProductCreateRequest request = new ProductCreateRequest(
        category.getId(),
        "테스트상품",
        "테스트 설명",
        "product/2025/08/demo.jpg",
        10_000L,
        ProductStatus.ON_SALE,
        5
    );

    // When
    ProductResponse resp = productService.createProduct(request);

    // Then
    assertThat(resp.id()).isNotNull();
    assertThat(resp.categoryId()).isEqualTo(category.getId());
    assertThat(resp.name()).isEqualTo("테스트상품");
    assertThat(resp.price()).isEqualTo(10_000L);
    assertThat(resp.quantity()).isEqualTo(5);
    assertThat(resp.imageUrl()).contains(resp.imageKey());

    // DB - 재고 확인
    Inventory inv = inventoryRepository.findById(resp.id()).orElseThrow();
    assertThat(inv.getQuantity()).isEqualTo(5);

    // DB - 로그 확인
    List<InventoryLog> logs = inventoryLogRepository.findAll();
    assertThat(logs).hasSize(1);
    assertThat(logs.getFirst().getReason()).isEqualTo(InventoryConstants.REASON_INITIAL);
    assertThat(logs.getFirst().getModifiedBy()).isEqualTo(adminId);

    // DB - 감사 컬럼 확인 (생성자/수정자)
    Product saved = productRepository.findById(resp.id()).orElseThrow();
    assertThat(saved.getCreatedBy()).isEqualTo(adminId);
    assertThat(saved.getUpdatedBy()).isEqualTo(adminId);

  }
}
