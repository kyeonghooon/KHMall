package com.khmall.category;

import com.khmall.AuthenticatedServiceTestBase;
import com.khmall.domain.category.CategoryRepository;
import com.khmall.domain.category.CategoryService;
import com.khmall.domain.category.dto.CategoryCreateRequest;
import com.khmall.domain.category.dto.CategoryResponse;
import com.khmall.domain.category.dto.CategoryUpdateRequest;
import com.khmall.exception.custom.DuplicateException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static com.khmall.common.constants.CategoryConstants.CATEGORY_NAME_DUPLICATE;
import static com.khmall.common.constants.CategoryConstants.CATEGORY_NOT_FOUND;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class CategoryUpdateTest extends AuthenticatedServiceTestBase {

  @Autowired
  private CategoryService categoryService;

  @Autowired
  private CategoryRepository categoryRepository;

  CategoryResponse parentCategory; // 상위 카테고리
  CategoryResponse childCategory;  // 하위 카테고리

  @BeforeEach
  void setUp() {
    String parentName = "TEST_PARENT";
    parentCategory = categoryService.createCategory(new CategoryCreateRequest(null, parentName, 10));

    String childName = "TEST_CHILD";
    childCategory = categoryService.createCategory(
        new CategoryCreateRequest(parentCategory.categoryId(), childName, 20));
  }

  @Test
  void 모든_필드_동시_수정_테스트() {
    // given
    CategoryUpdateRequest request = new CategoryUpdateRequest(
        JsonNullable.of("TEST_새이름"),
        JsonNullable.of(null), // 상위 카테고리 해제(최상위)
        JsonNullable.of(100)
    );

    // when
    CategoryResponse response = categoryService.updateCategory(childCategory.categoryId(), request);

    // then
    assertThat(response.name()).isEqualTo("TEST_새이름");
    assertThat(response.parentId()).isNull();
    assertThat(response.sortOrder()).isEqualTo(100);
  }

  @Test
  void 카테고리명만_변경시_나머지_값_유지_테스트() {
    // given
    CategoryUpdateRequest request = new CategoryUpdateRequest(
        JsonNullable.of("TEST_단일수정"),
        JsonNullable.undefined(),
        JsonNullable.undefined()
    );

    // when
    CategoryResponse response = categoryService.updateCategory(childCategory.categoryId(), request);

    // then
    // name은 변경, parentId/sortOrder는 기존 값 유지
    assertThat(response.name()).isEqualTo("TEST_단일수정");
    assertThat(response.parentId()).isEqualTo(childCategory.parentId());
    assertThat(response.sortOrder()).isEqualTo(childCategory.sortOrder());
  }

  @Test
  void 카테고리명_중복_수정시_DuplicateException() {
    // given
    String duplicateName = childCategory.name();
    CategoryUpdateRequest request = new CategoryUpdateRequest(
        JsonNullable.of(duplicateName),
        JsonNullable.undefined(),
        JsonNullable.undefined()
    );

    // when & then
    assertThatThrownBy(() -> categoryService.updateCategory(childCategory.categoryId(), request))
        .isInstanceOf(DuplicateException.class)
        .hasMessageContaining(CATEGORY_NAME_DUPLICATE);
  }

  @Test
  void 상위카테고리_미존재_NotFoundException() {
    // given
    Long nonExistentParentId = 9999L; // 존재하지 않는 상위 카테고리 ID
    CategoryUpdateRequest request = new CategoryUpdateRequest(
        JsonNullable.of("TEST_새이름"),
        JsonNullable.of(nonExistentParentId),
        JsonNullable.undefined()
    );

    // when & then
    assertThatThrownBy(() -> categoryService.updateCategory(childCategory.categoryId(), request))
        .isInstanceOf(com.khmall.exception.custom.NotFoundException.class)
        .hasMessageContaining(CATEGORY_NOT_FOUND);
  }

}
