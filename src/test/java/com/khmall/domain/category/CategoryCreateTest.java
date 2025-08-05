package com.khmall.domain.category;

import static com.khmall.common.constants.CategoryConstants.NAME_DUPLICATE;
import static com.khmall.common.constants.CategoryConstants.PARENT_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.khmall.common.constants.CategoryConstants;
import com.khmall.domain.category.dto.CategoryCreateRequest;
import com.khmall.domain.category.dto.CategoryResponse;
import com.khmall.exception.custom.BadRequestException;
import com.khmall.exception.custom.DuplicateException;
import com.khmall.exception.custom.NotFoundException;
import com.khmall.support.AuthenticatedServiceTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CategoryCreateTest extends AuthenticatedServiceTestBase {

  @Autowired
  private CategoryService categoryService;

  @Autowired
  private CategoryRepository categoryRepository;

  @Test
  void 카테고리_생성_테스트() {
    // given
    CategoryCreateRequest request = new CategoryCreateRequest(
        null, // 루트 카테고리
        "가구",
        0
    );

    // when
    CategoryResponse response = categoryService.createCategory(request);

    // then
    assertThat(response.categoryId()).isNotNull();
    assertThat(response.name()).isEqualTo("가구");
    assertThat(response.parentId()).isNull();
    assertThat(response.sortOrder()).isZero();

    Category savedCategory = categoryRepository.findById(response.categoryId())
        .orElseThrow(() -> new AssertionError("카테고리가 저장되지 않았습니다."));
    assertThat(savedCategory.getCreatedAt()).isNotNull();
    assertThat(savedCategory.getUpdatedAt()).isNotNull();
    assertThat(savedCategory.getCreatedBy()).isEqualTo(1L);
    assertThat(savedCategory.getUpdatedBy()).isEqualTo(1L);
  }

  @Test
  void 상위카테고리가_없으면_NotFoundException() {
    // given
    Long notExistParentId = 9999L; // DB에 없는 ID
    CategoryCreateRequest request = new CategoryCreateRequest(
        notExistParentId,
        "컴퓨터",
        0
    );

    // when & then
    assertThatThrownBy(() -> categoryService.createCategory(request))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining(PARENT_NOT_FOUND);
  }

  @Test
  void 중복된_이름_카테고리면_DuplicateException() {
    // given
    CategoryCreateRequest first = new CategoryCreateRequest(null, "패션", 0);
    categoryService.createCategory(first);

    CategoryCreateRequest dup = new CategoryCreateRequest(null, "패션", 1);

    // when & then
    assertThatThrownBy(() -> categoryService.createCategory(dup))
        .isInstanceOf(DuplicateException.class)
        .hasMessageContaining(NAME_DUPLICATE);
  }

  @Test
  void 카테고리_최대깊이_초과_테스트() {
    // given
    CategoryResponse rootCategory = categoryService.createCategory(new CategoryCreateRequest(null, "루트", 0));
    CategoryResponse parentCategory = categoryService.createCategory(new CategoryCreateRequest(rootCategory.categoryId(), "부모", 0));

    // 깊이 제한을 초과하는 카테고리 생성 요청
    CategoryCreateRequest request = new CategoryCreateRequest(parentCategory.categoryId(), "자식", 0);

    // when & then
    assertThatThrownBy(() -> categoryService.createCategory(request))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining(CategoryConstants.DEPTH_EXCEEDED);
  }
}
