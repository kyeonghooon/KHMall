package com.khmall.domain.product;

import com.khmall.common.constants.CategoryConstants;
import com.khmall.common.constants.ImageConstants;
import com.khmall.common.constants.InventoryConstants;
import com.khmall.config.S3Props;
import com.khmall.domain.category.Category;
import com.khmall.domain.category.CategoryRepository;
import com.khmall.domain.inventory.Inventory;
import com.khmall.domain.inventory.InventoryLogRepository;
import com.khmall.domain.inventory.InventoryMapper;
import com.khmall.domain.inventory.InventoryRepository;
import com.khmall.domain.product.dto.ProductCreateRequest;
import com.khmall.domain.product.dto.ProductResponse;
import com.khmall.exception.custom.BadRequestException;
import com.khmall.exception.custom.NotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@RequiredArgsConstructor
@Service
public class ProductService {

  private final S3Props props;
  private final S3Client s3;

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final InventoryRepository inventoryRepository;
  private final InventoryLogRepository inventoryLogRepository;

  @Transactional
  public ProductResponse createProduct(ProductCreateRequest request) {
    Category category = categoryRepository.findById(request.categoryId())
        .orElseThrow(() -> new NotFoundException(CategoryConstants.NOT_FOUND));

    try {
      HeadObjectResponse head = s3.headObject(b -> b.bucket(props.getBucket()).key(request.imageKey()));
      // 이미지 크기 및 형식 검증
      if (head.contentLength() > ImageConstants.MAX_SIZE) {
        // 이미지 크기가 초과되면 S3에서 객체 삭제
        s3.deleteObject(b -> b.bucket(props.getBucket()).key(request.imageKey()));
        throw new BadRequestException(ImageConstants.SIZE_EXCEEDED);
      }
      String contentType = head.contentType();
      if (!ImageConstants.ALLOWED_IMAGE_FORMATS.contains(contentType)) {
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

}
