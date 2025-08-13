package com.khmall.domain.product;

import com.khmall.common.constants.CategoryConstants;
import com.khmall.common.constants.ImageConstants;
import com.khmall.common.constants.InventoryConstants;
import com.khmall.common.constants.ProductConstants;
import com.khmall.config.S3Props;
import com.khmall.domain.category.Category;
import com.khmall.domain.category.CategoryRepository;
import com.khmall.domain.inventory.Inventory;
import com.khmall.domain.inventory.InventoryLogRepository;
import com.khmall.domain.inventory.InventoryMapper;
import com.khmall.domain.inventory.InventoryRepository;
import com.khmall.domain.product.dto.ProductCreateRequest;
import com.khmall.domain.product.dto.ProductResponse;
import com.khmall.domain.product.dto.ProductUpdateRequest;
import com.khmall.exception.custom.BadRequestException;
import com.khmall.exception.custom.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService {

  private final S3Props props;
  private final S3Client s3;

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final InventoryRepository inventoryRepository;
  private final InventoryLogRepository inventoryLogRepository;

  /**
   * 상품 생성
   *
   * @param request 상품 생성 요청
   * @return 생성된 상품 정보
   */
  @Transactional
  public ProductResponse createProduct(ProductCreateRequest request) {
    Category category = categoryRepository.findById(request.categoryId())
        .orElseThrow(() -> new NotFoundException(CategoryConstants.NOT_FOUND));

    verificationImageKey(request.imageKey());

    Product product = ProductMapper.toEntity(request, category);

    Product savedProduct = productRepository.save(product);

    Inventory inventory = new Inventory(savedProduct, request.quantity());
    inventoryRepository.save(inventory);

    inventoryLogRepository.save(
        InventoryMapper.toLogEntity(savedProduct, request.quantity(), InventoryConstants.REASON_INITIAL)
    );

    String imageUrl = props.getBaseUrl() + savedProduct.getImageKey();
    return ProductMapper.toResponse(
        savedProduct, request.quantity(), imageUrl
    );
  }

  @Transactional
  public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new NotFoundException(ProductConstants.NOT_FOUND));

    updateCategory(product, request);
    updateName(product, request);
    updateDescription(product, request);
    updateImageKey(product, request);
    updatePrice(product, request);
    updateStatus(product, request);
    updateQuantity(product, request);

    String imageUrl = props.getBaseUrl() + product.getImageKey();
    Inventory inventory = inventoryRepository.findById(product.getId())
        .orElseThrow(() -> new NotFoundException(ProductConstants.NOT_FOUND));

    return ProductMapper.toResponse(
        product, inventory.getQuantity(), imageUrl
    );
  }

  public void verificationImageKey(String imageKey) {
    try {
      HeadObjectResponse head = s3.headObject(b -> b.bucket(props.getBucket()).key(imageKey));
      // 이미지 크기 및 형식 검증
      if (head.contentLength() > ImageConstants.MAX_SIZE) {
        // 이미지 크기가 초과되면 S3에서 객체 삭제
        s3.deleteObject(b -> b.bucket(props.getBucket()).key(imageKey));
        throw new BadRequestException(ImageConstants.SIZE_EXCEEDED);
      }
      // 0바이트 이미지 검증
      if (head.contentLength() == 0) {
        // 0바이트 이미지인 경우 S3에서 객체 삭제
        s3.deleteObject(b -> b.bucket(props.getBucket()).key(imageKey));
        throw new BadRequestException(ImageConstants.EMPTY_IMAGE);
      }
      String contentType = head.contentType();
      if (!ImageConstants.ALLOWED_IMAGE_FORMATS.contains(contentType)) {
        // 지원하지 않는 이미지 형식인 경우 S3에서 객체 삭제
        s3.deleteObject(b -> b.bucket(props.getBucket()).key(imageKey));
        throw new BadRequestException(ImageConstants.UNSUPPORTED_FORMAT);
      }
    } catch (S3Exception e) {
      // S3에서 객체를 찾을 수 없는 경우
      if (e.statusCode() == HttpStatus.NOT_FOUND.value()) {
        throw new BadRequestException(ImageConstants.IMAGE_NOT_REGISTERED);
      }
      // 다른 S3 오류 발생 시
      throw e;
    }
  }

  private void updateCategory(Product product, ProductUpdateRequest request) {
    if (!request.categoryId().isPresent()) {
      return; // 카테고리 변경이 없는 경우
    }
    Long categoryId = request.categoryId().get();
    if (categoryId == null) {
      throw new BadRequestException(ProductConstants.CATEGORY_NOT_BLANK_MESSAGE);
    }
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new NotFoundException(CategoryConstants.NOT_FOUND));
    product.setCategory(category);
  }

  private void updateName(Product product, ProductUpdateRequest request) {
    if (!request.name().isPresent()) {
      return; // 이름 변경이 없는 경우
    }
    String name = request.name().get();
    ProductValidator.validateProductName(name);
    product.setName(name);
  }

  private void updateDescription(Product product, ProductUpdateRequest request) {
    if (!request.description().isPresent()) {
      return; // 설명 변경이 없는 경우
    }
    String description = request.description().get();
    // null 일경우 null로 설정
    product.setDescription(description);
  }

  private void updateImageKey(Product product, ProductUpdateRequest request) {
    if (!request.imageKey().isPresent()) {
      return; // 이미지 키 변경이 없는 경우
    }
    String newKey = request.imageKey().get();
    if (newKey == null || newKey.isBlank()) {
      throw new BadRequestException(ProductConstants.IMAGE_KEY_NOT_BLANK_MESSAGE);
    }
    if (!newKey.matches(ProductConstants.IMAGE_KEY_PATTERN)) {
      throw new BadRequestException(ProductConstants.IMAGE_KEY_PATTERN_MESSAGE);
    }

    // 기존 이미지 키와 동일한 경우 변경하지 않음
    String oldKey = product.getImageKey();
    if (newKey.equals(oldKey)) {
      return;
    }

    // 이미지 키 검증
    verificationImageKey(newKey);

    // 새로운 이미지 키 설정
    product.setImageKey(newKey);

    // S3에서 기존 이미지 삭제
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            if (oldKey != null && !oldKey.isBlank()) {
              try {
                s3.deleteObject(b -> b.bucket(props.getBucket()).key(oldKey));
              } catch (S3Exception e) {
                // S3에서 객체 삭제 실패 시 로깅 또는 예외 처리
                log.error("Failed to delete old image key from S3: {}", oldKey, e);
              }
            }
          }
        }
    );

  }

  private void updatePrice(Product product, ProductUpdateRequest request) {
    if (!request.price().isPresent()) {
      return; // 가격 변경이 없는 경우
    }
    Long price = request.price().get();
    if (price == null) {
      throw new BadRequestException(ProductConstants.PRICE_NOT_BLANK_MESSAGE);
    }
    if (price < 0) {
      throw new BadRequestException(ProductConstants.PRICE_MIN_MESSAGE);
    }
    product.setPrice(price);
  }

  private void updateStatus(Product product, ProductUpdateRequest request) {
    if (!request.status().isPresent()) {
      return; // 상태 변경이 없는 경우
    }
    ProductStatus status = request.status().get();
    if (status == null) {
      throw new BadRequestException(ProductConstants.STATUS_NOT_BLANK_MESSAGE);
    }
    product.setStatus(status);
  }

  private void updateQuantity(Product product, ProductUpdateRequest request) {
    if (!request.quantity().isPresent()) {
      return; // 수량 변경이 없는 경우
    }
    Integer quantity = request.quantity().get();
    if (quantity == null) {
      throw new BadRequestException(ProductConstants.QUANTITY_NOT_BLANK_MESSAGE);
    }
    if (quantity < 0) {
      throw new BadRequestException(ProductConstants.QUANTITY_MIN_MESSAGE);
    }

    // 재고 업데이트
    Inventory inventory = inventoryRepository.findById(product.getId())
        .orElseThrow(() -> new NotFoundException(ProductConstants.NOT_FOUND));

    // 재고 수량 변경
    inventory.overwrite(quantity);

    // 재고 로그 기록
    inventoryLogRepository.save(
        InventoryMapper.toLogEntity(product, quantity, InventoryConstants.REASON_OVERWRITE)
    );
  }

}
