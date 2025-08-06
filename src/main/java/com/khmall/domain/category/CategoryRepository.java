package com.khmall.domain.category;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  // 상위 카테고리와 이름으로 카테고리 존재 여부 확인
  boolean existsByParentAndName(Category parent, String name);

  // 해당 id를 상위 카테고리로 가지는 카테고리 존재 여부 확인
  boolean existsByParent_CategoryId(Long parentId);

}