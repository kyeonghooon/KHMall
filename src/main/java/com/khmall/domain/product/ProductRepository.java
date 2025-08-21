package com.khmall.domain.product;

import com.khmall.domain.product.dto.AdminProductDetailResponse;
import com.khmall.domain.product.dto.AdminProductListView;
import com.khmall.domain.product.dto.CustomerProductDetailResponse;
import com.khmall.domain.product.dto.CustomerProductListView;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductQueryRepository {

  // 특정 카테고리의 상품 존재 확인
  boolean existsByCategory_Id(Long categoryId);



  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Product p where p.id = :id")
  Optional<Product> findByIdForUpdate(@Param("id") Long id);
}