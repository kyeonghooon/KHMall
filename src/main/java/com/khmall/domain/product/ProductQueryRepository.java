package com.khmall.domain.product;

import com.khmall.domain.product.dto.AdminProductDetailResponse;
import com.khmall.domain.product.dto.AdminProductListView;
import com.khmall.domain.product.dto.CustomerProductDetailResponse;
import com.khmall.domain.product.dto.CustomerProductListView;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductQueryRepository {
  Optional<AdminProductDetailResponse> findAdminProductDetailById(Long id);

  Page<AdminProductListView> findAdminProductList(
      String keyword,
      ProductStatus status,
      boolean noCategoryFilter,
      Collection<Long> categoryIds,
      Pageable pageable
  );

  Optional<CustomerProductDetailResponse> findCustomerProductDetailById(Long id);

  Page<CustomerProductListView> findCustomerProductList(
      String keyword,
      boolean noCategoryFilter,
      Collection<Long> categoryIds,
      Pageable pageable
  );
}
