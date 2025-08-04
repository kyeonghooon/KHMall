package com.khmall.domain.category;

import static com.khmall.common.constants.CategoryConstants.CATEGORY_NAME_DUPLICATE;
import static com.khmall.common.constants.CategoryConstants.CATEGORY_NOT_FOUND;

import com.khmall.domain.category.dto.CategoryCreateRequest;
import com.khmall.domain.category.dto.CategoryResponse;
import com.khmall.exception.custom.DuplicateException;
import com.khmall.exception.custom.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CategoryService {

  private final CategoryRepository categoryRepository;

  /**
   * 카테고리를 생성합니다.
   *
   * @param request 카테고리 생성 요청
   * @return 생성된 카테고리 정보
   * @throws NotFoundException 상위 카테고리가 존재하지 않을 경우
   * @throws DuplicateException 이미 존재하는 이름의 카테고리일 경우
   */
  public CategoryResponse createCategory(CategoryCreateRequest request) {
    Category parent = null;
    if (request.parentId() != null) {
      parent = categoryRepository.findById(request.parentId())
          .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND));
    }

    // 상위 카테고리와 이름으로 카테고리 존재 여부 확인
    if (categoryRepository.existsByParentAndName(parent, request.name())) {
      throw new DuplicateException(CATEGORY_NAME_DUPLICATE);
    }

    // 카테고리 엔티티 생성
    Category category = CategoryMapper.toEntity(request, parent);

    // 카테고리 저장
    Category savedCategory = categoryRepository.save(category);

    // 저장된 카테고리 응답으로 변환
    return CategoryMapper.toResponse(savedCategory);
  }

}
