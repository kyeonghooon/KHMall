package com.khmall.domain.category;


import com.khmall.common.constants.CategoryConstants;
import com.khmall.domain.category.dto.CategoryCreateRequest;
import com.khmall.domain.category.dto.CategoryDeleteResult;
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
  private final CategoryValidator categoryValidator;

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
          .orElseThrow(() -> new NotFoundException(CategoryConstants.PARENT_NOT_FOUND));
    }

    // 상위 카테고리와 이름으로 카테고리 존재 여부 확인
    categoryValidator.validateDuplicateName(parent, request.name());
    // 카테고리 깊이 제한 검증
    categoryValidator.validateDepth(parent);

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
   * @throws NotFoundException   카테고리가 존재하지 않을 경우
   * @throws BadRequestException 잘못된 요청일 경우 (예: 이름이 비어있거나 길이가 초과)
   * @throws DuplicateException  이미 존재하는 이름의 카테고리일 경우
   */
  @Transactional
  public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(CategoryConstants.NOT_FOUND));

    // 변경 전 값 백업
    String prevName = category.getName();
    Long prevParentId = category.getParent() == null ? null : category.getParent().getCategoryId();
    Integer prevSortOrder = category.getSortOrder();

    updateCategoryName(category, request);
    updateParentCategory(category, request);
    updateSortOrder(category, request);

    logCategoryChange(id, prevName, category.getName(), prevParentId,
        category.getParent() == null ? null : category.getParent().getCategoryId(),
        prevSortOrder, category.getSortOrder());

    return CategoryMapper.toResponse(category);
  }

  @Transactional
  public CategoryDeleteResult deleteCategory(Long id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(CategoryConstants.NOT_FOUND));

    // 하위 카테고리 존재 검사
    if (categoryRepository.existsByParent_CategoryId(id)) {
      throw new BadRequestException(CategoryConstants.CHILDREN_EXIST);
    }

    // TODO 연결된 상품 존재 검사

    categoryRepository.deleteById(id);

    // 로그용 리턴값
    return new CategoryDeleteResult(
        category.getCategoryId(),
        category.getName()
    );
  }

  private void updateCategoryName(Category category, CategoryUpdateRequest request) {
    if (!request.name().isPresent()) {
      return; // 이름 변경이 없는 경우
    }
    String name = request.name().get();
    validateName(name);
    // 상위 카테고리를 변경 하지 않을 경우 중복 확인
    if (!request.parentId().isPresent()) {
      categoryValidator.validateDuplicateName(category.getParent(), name);
    }
    category.setName(name);
  }

  private void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new BadRequestException(CategoryConstants.NAME_NOT_BLANK_MESSAGE);
    }
    if (name.length() > CategoryConstants.NAME_MAX_LENGTH) {
      throw new BadRequestException(CategoryConstants.NAME_SIZE_MESSAGE);
    }
  }

  private void updateParentCategory(Category category, CategoryUpdateRequest request) {
    if (!request.parentId().isPresent()) {
      return; // 부모 카테고리 변경이 없는 경우
    }
    Long parentId = request.parentId().get();
    // 부모 카테고리 ID가 null인 경우 최상단 카테고리로 설정
    if (parentId == null) {
      // 최상단 카테고리에 동일 이름 있는지 확인
      categoryValidator.validateDuplicateName(null, category.getName());
      category.setParent(null);
    }
    // 자기 참조 방지
    else if (parentId.equals(category.getCategoryId())) {
      throw new BadRequestException(CategoryConstants.SELF_REFERENCE_MESSAGE);
    }
    // 상위 카테고리를 변경 하려는 경우
    else {
      Category parent = categoryRepository.findById(parentId)
          .orElseThrow(() -> new NotFoundException(CategoryConstants.PARENT_NOT_FOUND));
      // 카테고리 최대 깊이 체크
      categoryValidator.validateDepth(parent);
      // 상위 카테고리와 이름으로 카테고리 존재 여부 확인
      categoryValidator.validateDuplicateName(parent, category.getName());
      category.setParent(parent);
    }
  }

  private void updateSortOrder(Category category, CategoryUpdateRequest request) {
    if (!request.sortOrder().isPresent()) {
      return; // 정렬 순서 변경이 없는 경우
    }
    Integer sortOrder = request.sortOrder().get();
    if (sortOrder == null || sortOrder < 0) {
      throw new BadRequestException(CategoryConstants.SORT_ORDER_MIN_MESSAGE);
    }
    category.setSortOrder(sortOrder);
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
