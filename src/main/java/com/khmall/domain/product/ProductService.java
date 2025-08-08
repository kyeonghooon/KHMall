package com.khmall.domain.product;

import com.khmall.common.constants.CategoryConstants;
import com.khmall.common.constants.InventoryConstants;
import com.khmall.domain.category.Category;
import com.khmall.domain.category.CategoryRepository;
import com.khmall.domain.inventory.Inventory;
import com.khmall.domain.inventory.InventoryLogRepository;
import com.khmall.domain.inventory.InventoryMapper;
import com.khmall.domain.inventory.InventoryRepository;
import com.khmall.domain.product.dto.ProductCreateRequest;
import com.khmall.domain.product.dto.ProductResponse;
import com.khmall.exception.custom.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final InventoryRepository inventoryRepository;
  private final InventoryLogRepository inventoryLogRepository;

  @Transactional
  public ProductResponse createProduct(ProductCreateRequest request) {
    Category category = categoryRepository.findById(request.categoryId())
        .orElseThrow(() -> new NotFoundException(CategoryConstants.NOT_FOUND));

    Product product = ProductMapper.toEntity(request, category);

    Product savedProduct = productRepository.save(product);

    Inventory inventory = new Inventory(savedProduct, request.quantity());
    inventoryRepository.save(inventory);

    inventoryLogRepository.save(
        InventoryMapper.toLogEntity(savedProduct, request.quantity(), InventoryConstants.REASON_INITIAL)
    );

    // TODO baseURL
    String imageUrl = "http://example.com/images/" + savedProduct.getImageKey();
    return ProductMapper.toResponse(
        savedProduct, request.quantity(), imageUrl
    );
  }

}
