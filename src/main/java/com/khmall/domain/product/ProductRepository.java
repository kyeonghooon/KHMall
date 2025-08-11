package com.khmall.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

  // 특정 카테고리의 상품 존재 확인
  boolean existsByCategoryId(Long categoryId);
}