package com.khmall.domain.category;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  // 상위 카테고리와 이름으로 카테고리 존재 여부 확인
  boolean existsByParentAndName(Category parent, String name);

  // 해당 id를 상위 카테고리로 가지는 카테고리 존재 여부 확인
  boolean existsByParent_Id(Long parentId);

  interface Flat {
    Long getId();
    String getName();
    Long getParentId();
  }

  @Query("""
    select c.id as id,
           c.name       as name,
           c.parent.id as parentId
    from Category c
  """)
  List<Flat> findAllFlat();

}