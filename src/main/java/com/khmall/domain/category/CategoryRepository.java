package com.khmall.domain.category;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  // 상위 카테고리와 이름으로 카테고리 존재 여부 확인
  boolean existsByParentAndName(Category parent, String name);

}