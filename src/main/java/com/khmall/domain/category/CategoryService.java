package com.khmall.domain.category;

import static com.khmall.common.constants.CategoryConstants.CATEGORY_NAME_DUPLICATE;
import static com.khmall.common.constants.CategoryConstants.CATEGORY_NOT_FOUND;
import static com.khmall.common.constants.CategoryConstants.NAME_MAX_LENGTH;
import static com.khmall.common.constants.CategoryConstants.NAME_NOT_BLANK_MESSAGE;
import static com.khmall.common.constants.CategoryConstants.NAME_SIZE_MESSAGE;
import static com.khmall.common.constants.CategoryConstants.SELF_REFERENCE_MESSAGE;
import static com.khmall.common.constants.CategoryConstants.SORT_ORDER_MIN_MESSAGE;

import com.khmall.domain.category.dto.CategoryCreateRequest;
import com.khmall.domain.category.dto.CategoryResponse;
import com.khmall.domain.category.dto.CategoryUpdateRequest;
import com.khmall.exception.custom.BadRequestException;
import com.khmall.exception.custom.DuplicateException;
import com.khmall.exception.custom.NotFoundException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryService {

  private final CategoryRepository categoryRepository;

  /**
   * 카테고리를 생성합니다.
   *
   * @param request 카테고리 생성 요청
   * @return 생성된 카테고리 정보
   * @throws NotFoundException  상위 카테고리가 존재하지 않을 경우
   * @throws DuplicateException 이미 존재하는 이름의 카테고리일 경우
   */
  @Transactional
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

  /**
   * 카테고리를 수정합니다.
   *
   * @param id      수정할 카테고리 ID
   * @param request 카테고리 수정 요청
   * @return 수정된 카테고리 정보
   * @throws NotFoundException  카테고리가 존재하지 않을 경우
   * @throws BadRequestException 잘못된 요청일 경우 (예: 이름이 비어있거나 길이가 초과)
   * @throws DuplicateException 이미 존재하는 이름의 카테고리일 경우
   */
  @Transactional
  public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND));

    // 변경 전 값 백업
    String prevName = category.getName();
    Long prevParentId = category.getParent() == null ? null : category.getParent().getCategoryId();
    Integer prevSortOrder = category.getSortOrder();

    updateCategoryName(category, request);
    updateParentCategory(category, request, id);
    updateSortOrder(category, request);

    logCategoryChange(id, prevName, category.getName(), prevParentId,
        category.getParent() == null ? null : category.getParent().getCategoryId(),
        prevSortOrder, category.getSortOrder());

    return CategoryMapper.toResponse(category);
  }

  private void updateCategoryName(Category category, CategoryUpdateRequest request) {
    request.name().ifPresent(name -> {
      validateName(name);
      if (categoryRepository.existsByParentAndName(category.getParent(), name)) {
        throw new DuplicateException(CATEGORY_NAME_DUPLICATE);
      }
      category.setName(name);
    });
  }

  private void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new BadRequestException(NAME_NOT_BLANK_MESSAGE);
    }
    if (name.length() > NAME_MAX_LENGTH) {
      throw new BadRequestException(NAME_SIZE_MESSAGE);
    }
  }

  private void updateParentCategory(Category category, CategoryUpdateRequest request, Long id) {
    request.parentId().ifPresent(parentId -> {
      if (parentId == null) {
        category.setParent(null);
      } else if (parentId.equals(id)) {
        throw new BadRequestException(SELF_REFERENCE_MESSAGE);
      } else {
        Category parent = categoryRepository.findById(parentId)
            .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND));
        category.setParent(parent);
      }
    });
  }

  private void updateSortOrder(Category category, CategoryUpdateRequest request) {
    request.sortOrder().ifPresent(sortOrder -> {
      if (sortOrder == null || sortOrder < 0) {
        throw new BadRequestException(SORT_ORDER_MIN_MESSAGE);
      }
      category.setSortOrder(sortOrder);
    });
  }

  private void logCategoryChange(Long id, String prevName, String newName,
      Long prevParentId, Long newParentId,
      Integer prevSortOrder, Integer newSortOrder) {
    if (!Objects.equals(prevName, newName)) {
      log.info("[카테고리수정][ID={}] name: '{}' → '{}'", id, prevName, newName);
    }
    if (!Objects.equals(prevParentId, newParentId)) {
      log.info("[카테고리수정][ID={}] parentId: {} → {}", id, prevParentId, newParentId);
    }
    if (!Objects.equals(prevSortOrder, newSortOrder)) {
      log.info("[카테고리수정][ID={}] sortOrder: {} → {}", id, prevSortOrder, newSortOrder);
    }
  }
}
