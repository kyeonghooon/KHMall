package com.khmall.domain.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.khmall.common.constants.InventoryConstants;
import com.khmall.domain.category.Category;
import com.khmall.domain.category.CategoryRepository;
import com.khmall.domain.inventory.Inventory;
import com.khmall.domain.inventory.InventoryLog;
import com.khmall.domain.inventory.InventoryLogRepository;
import com.khmall.domain.inventory.InventoryRepository;
import com.khmall.domain.product.dto.ProductCreateRequest;
import com.khmall.domain.product.dto.ProductUpdateRequest;
import com.khmall.exception.custom.BadRequestException;
import com.khmall.support.AuthenticatedServiceTestBase;
import com.khmall.util.TestEntityFactory;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

class ProductUpdateTest extends AuthenticatedServiceTestBase {

  @Autowired
  ProductService productService;
  @Autowired
  ProductRepository productRepository;
  @Autowired
  CategoryRepository categoryRepository;
  @Autowired
  InventoryRepository inventoryRepository;
  @Autowired
  InventoryLogRepository inventoryLogRepository;

  @MockitoBean
  S3Client s3; // 서비스에서 주입받는 S3Client 목

  private static HeadObjectResponse headOK() {
    return HeadObjectResponse.builder()
        .contentLength(1_024L)
        .contentType("image/jpeg")
        .build();
  }

  private static HeadObjectResponse headZero() {
    return HeadObjectResponse.builder()
        .contentLength(0L)
        .contentType("image/jpeg")
        .build();
  }

  private static String uuid32() {
    return java.util.UUID.randomUUID().toString().replace("-", "");
  }

  @BeforeEach
  void setUp() {
    // 기본적으로 모든 headObject 호출은 정상 응답으로 처리
    when(s3.headObject(any(Consumer.class))).thenReturn(headOK());
  }

  private Product seedProduct(String imageKey) {
    Category cat = TestEntityFactory.createCategory(categoryRepository, null, "카테고리A", 0);
    ProductCreateRequest req = new ProductCreateRequest(
        cat.getId(),
        "원래이름",
        "원래설명",
        imageKey,
        10_000L,
        ProductStatus.ON_SALE,
        5
    );
    return productRepository.findById(productService.createProduct(req).id()).orElseThrow();
  }

  @Test
  @DisplayName("상품 부분수정: 이름/카테고리/가격/설명/상태/수량 성공")
  void 부분수정_성공() {
    // Given
    String uuid = uuid32();
    Product p = seedProduct("products/2025/08/" + uuid + ".jpg");
    Category newCat = TestEntityFactory.createCategory(categoryRepository, null, "카테고리B", 0);

    ProductUpdateRequest patch = new ProductUpdateRequest(
        JsonNullable.of(newCat.getId()),      // categoryId
        JsonNullable.of("새이름"),              // name
        JsonNullable.of("새설명"),              // description
        JsonNullable.undefined(),             // imageKey (변경 없음)
        JsonNullable.of(20_000L),             // price
        JsonNullable.of(ProductStatus.STOP),// status
        JsonNullable.of(20)                   // quantity
    );

    // When
    productService.updateProduct(p.getId(), patch);

    // Then
    Product saved = productRepository.findById(p.getId()).orElseThrow();
    assertThat(saved.getCategory().getId()).isEqualTo(newCat.getId());
    assertThat(saved.getName()).isEqualTo("새이름");
    assertThat(saved.getDescription()).isEqualTo("새설명");
    assertThat(saved.getPrice()).isEqualTo(20_000L);
    assertThat(saved.getStatus()).isEqualTo(ProductStatus.STOP);

    Inventory inv = inventoryRepository.findById(p.getId()).orElseThrow();
    assertThat(inv.getQuantity()).isEqualTo(20);

    List<InventoryLog> logs = inventoryLogRepository.findAll();
    assertThat(logs).hasSize(2); // 초기 등록 1 + 덮어쓰기 1
    assertThat(logs.get(1).getReason()).isEqualTo(InventoryConstants.REASON_OVERWRITE);
  }

  @Test
  @DisplayName("이미지 변경 없음: 동일 imageKey 전달 시 S3 headObject 호출 없이 no-op")
  void 이미지_동일키_noop() {
    // Given
    String uuid = uuid32();
    System.out.println("UUID: " + uuid);
    Product p = seedProduct("products/2025/08/" + uuid + ".jpg");

    // reset: create 단계의 head 호출 카운트를 리셋
    reset(s3);

    ProductUpdateRequest patch = new ProductUpdateRequest(
        JsonNullable.undefined(),             // categoryId
        JsonNullable.undefined(),             // name
        JsonNullable.undefined(),             // description
        JsonNullable.of("products/2025/08/" + uuid + ".jpg"), // imageKey (동일)
        JsonNullable.undefined(),             // price
        JsonNullable.undefined(),             // status
        JsonNullable.undefined()              // quantity
    );

    // When
    productService.updateProduct(p.getId(), patch);

    // Then: 동일 키라면 검증(HEAD) 자체가 호출되지 않아야 한다
    verify(s3, times(0)).headObject(any(Consumer.class));
    Product saved = productRepository.findById(p.getId()).orElseThrow();
    assertThat(saved.getImageKey()).isEqualTo("products/2025/08/" + uuid + ".jpg");
  }

  @Test
  @DisplayName("이미지 변경: 새 imageKey 전달 시 검증(HEAD) 수행되고 이미지 키 갱신")
  void 이미지_변경_검증후_갱신() {
    // Given
    String oldUuid = uuid32();
    Product p = seedProduct("products/2025/08/" + oldUuid + ".jpg");

    reset(s3);
    // 새 키 검증 응답 설정
    when(s3.headObject(any(Consumer.class))).thenReturn(headOK());

    String newUuid = uuid32();
    String newKey = "products/2025/08/" + newUuid + ".jpg";
    ProductUpdateRequest patch = new ProductUpdateRequest(
        JsonNullable.undefined(),             // categoryId
        JsonNullable.undefined(),             // name
        JsonNullable.undefined(),             // description
        JsonNullable.of(newKey),              // imageKey
        JsonNullable.undefined(),             // price
        JsonNullable.undefined(),             // status
        JsonNullable.undefined()              // quantity
    );

    // When
    productService.updateProduct(p.getId(), patch);

    // Then
    // headObject 1회 호출 확인
    verify(s3, times(1)).headObject(any(Consumer.class));

    // (afterCommit 삭제는 테스트 트랜잭션이 롤백이므로 여기선 호출되지 않는 게 정상)
    verify(s3, never()).deleteObject((DeleteObjectRequest) any());

    Product saved = productRepository.findById(p.getId()).orElseThrow();
    assertThat(saved.getImageKey()).isEqualTo(newKey);
  }

  @Test
  @DisplayName("가격 음수면 예외")
  void 가격_음수_예외() {
    // Given
    Product p = seedProduct("products/2025/08/old.jpg");

    ProductUpdateRequest patch = new ProductUpdateRequest(
        JsonNullable.undefined(),             // categoryId
        JsonNullable.undefined(),             // name
        JsonNullable.undefined(),             // description
        JsonNullable.undefined(),             // imageKey
        JsonNullable.of(-1L),                 // price
        JsonNullable.undefined(),             // status
        JsonNullable.undefined()              // quantity
    );

    // Expect
    assertThatThrownBy(() -> productService.updateProduct(p.getId(), patch))
        .isInstanceOf(BadRequestException.class);

    // Then: 값 불변
    Product saved = productRepository.findById(p.getId()).orElseThrow();
    assertThat(saved.getPrice()).isEqualTo(10_000L);
  }

  @Test
  @DisplayName("수량 음수면 예외")
  void 수량_음수_예외() {
    // Given
    Product p = seedProduct("products/2025/08/old.jpg");

    ProductUpdateRequest patch = new ProductUpdateRequest(
        JsonNullable.undefined(),             // categoryId
        JsonNullable.undefined(),             // name
        JsonNullable.undefined(),             // description
        JsonNullable.undefined(),             // imageKey
        JsonNullable.undefined(),             // price
        JsonNullable.undefined(),             // status
        JsonNullable.of(-5)                   // quantity
    );

    // Expect
    assertThatThrownBy(() -> productService.updateProduct(p.getId(), patch))
        .isInstanceOf(BadRequestException.class);

    // Then: 재고 불변 & 로그 추가 없음
    Inventory inv = inventoryRepository.findById(p.getId()).orElseThrow();
    assertThat(inv.getQuantity()).isEqualTo(5);
    assertThat(inventoryLogRepository.findAll()).hasSize(1); // 초기 생성 로그만
  }

  @Test
  @DisplayName("설명 null 허용: null로 업데이트 가능")
  void 설명_null_허용() {
    // Given
    Product p = seedProduct("products/2025/08/old.jpg");

    ProductUpdateRequest patch = new ProductUpdateRequest(
        JsonNullable.undefined(),             // categoryId
        JsonNullable.undefined(),             // name
        JsonNullable.of(null),                // description -> null
        JsonNullable.undefined(),             // imageKey
        JsonNullable.undefined(),             // price
        JsonNullable.undefined(),             // status
        JsonNullable.undefined()              // quantity
    );

    // When
    productService.updateProduct(p.getId(), patch);

    // Then
    Product saved = productRepository.findById(p.getId()).orElseThrow();
    assertThat(saved.getDescription()).isNull();
  }

  @Test
  @DisplayName("이미지 0바이트 업로드는 거부")
  void 이미지_0바이트_거부() {
    // Given
    Product p = seedProduct("products/2025/08/old.jpg");

    reset(s3);
    when(s3.headObject(any(Consumer.class))).thenReturn(headZero());

    ProductUpdateRequest patch = new ProductUpdateRequest(
        JsonNullable.undefined(),             // categoryId
        JsonNullable.undefined(),             // name
        JsonNullable.undefined(),             // description
        JsonNullable.of("products/2025/08/zero.jpg"),
        JsonNullable.undefined(),             // price
        JsonNullable.undefined(),             // status
        JsonNullable.undefined()              // quantity
    );

    // Expect
    assertThatThrownBy(() -> productService.updateProduct(p.getId(), patch))
        .isInstanceOf(BadRequestException.class);
  }
}
