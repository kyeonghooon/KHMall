package com.khmall.domain.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.khmall.common.constants.CategoryConstants;
import com.khmall.common.constants.CommonConstants;
import com.khmall.domain.category.dto.CategoryDeleteResult;
import com.khmall.exception.custom.ConflictException;
import com.khmall.exception.custom.NotFoundException;
import com.khmall.support.AuthenticatedServiceTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CategoryDeleteTest extends AuthenticatedServiceTestBase {

  @Autowired
  private CategoryService categoryService;

  @Autowired
  private CategoryRepository categoryRepository;

  private Category category;

  @BeforeEach
  void setUp() {
    // 항상 부모 카테고리 하나만 준비
    category = categoryRepository.save(
        Category.builder()
            .name("부모 카테고리")
            .sortOrder(1)
            .build()
    );
  }

  @Test
  void 정상_삭제_성공() {
    // when
    CategoryDeleteResult result = categoryService.deleteCategory(category.getId());

    // then
    assertThat(result.categoryId()).isEqualTo(category.getId());
    assertThat(result.name()).isEqualTo(category.getName());
    assertThat(result.message()).isEqualTo(CommonConstants.DELETE_SUCCESS);

    // 삭제 후 DB에 존재하지 않아야 함
    assertThat(categoryRepository.findById(category.getId())).isEmpty();
  }

  @Test
  void 존재하지_않는_카테고리_삭제시_예외() {
    // when + then
    assertThatThrownBy(() -> categoryService.deleteCategory(9999L))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining(CategoryConstants.NOT_FOUND);
  }

  @Test
  void 하위_카테고리_존재시_삭제_예외() {
    // given: 자식 카테고리 추가
    categoryRepository.save(
        Category.builder()
            .name("자식 카테고리")
            .sortOrder(1)
            .parent(category)
            .build()
    );

    // when + then
    assertThatThrownBy(() -> categoryService.deleteCategory(category.getId()))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining(CategoryConstants.CHILDREN_EXIST);
  }
}
