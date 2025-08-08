package com.khmall.domain.category;


import com.khmall.common.constants.CategoryConstants;
import com.khmall.domain.category.dto.CategoryCreateRequest;
import com.khmall.domain.category.dto.CategoryDeleteResult;
import com.khmall.domain.category.dto.CategoryResponse;
import com.khmall.domain.category.dto.CategoryTreeResponse;
import com.khmall.domain.category.dto.CategoryUpdateRequest;
import com.khmall.exception.custom.BadRequestException;
import com.khmall.exception.custom.ConflictException;
import com.khmall.exception.custom.DuplicateException;
import com.khmall.exception.custom.NotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    updateCategoryName(category, request);
    updateParentCategory(category, request);
    updateSortOrder(category, request);

    return CategoryMapper.toResponse(category);
  }

  /**
   * 카테고리를 삭제합니다.
   *
   * @param id 삭제할 카테고리 ID
   * @return 삭제된 카테고리 정보
   * @throws NotFoundException    카테고리가 존재하지 않을 경우
   * @throws BadRequestException 하위 카테고리가 존재하는 경우
   */
  @Transactional
  public CategoryDeleteResult deleteCategory(Long id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(CategoryConstants.NOT_FOUND));

    // 하위 카테고리 존재 검사
    if (categoryRepository.existsByParent_CategoryId(id)) {
      throw new ConflictException(CategoryConstants.CHILDREN_EXIST);
    }

    // TODO 연결된 상품 존재 검사

    categoryRepository.deleteById(id);

    // 로그용 리턴값
    return new CategoryDeleteResult(
        category.getId(),
        category.getName()
    );
  }

  /**
   * 카테고리 트리를 조회합니다.
   *
   * @return 카테고리 트리 구조
   */
  public List<CategoryTreeResponse> getCategoryTree() {
    List<Category> categories = categoryRepository.findAll();

    // id -> DTO 매핑
    Map<Long, CategoryTreeResponse> dtoMap = categories.stream()
        .collect(Collectors.toMap(
            Category::getId,
            CategoryMapper::toTreeResponse
        ));

    // tree 구조로 변환
    categories.forEach(category -> {
      if (category.getParent() != null) {
        Long parentId = category.getParent().getId();
        CategoryTreeResponse parentDto = dtoMap.get(parentId);
        if (parentDto != null) {
          parentDto.children().add(dtoMap.get(category.getId()));
        }
      }
    });

    // 최상위 카테고리만 추출
    List<CategoryTreeResponse> roots = categories.stream()
        .filter(c -> c.getParent() == null)
        .map(c -> dtoMap.get(c.getId()))
        .collect(Collectors.toList());

    // 자식 카테고리 정렬
    sortChildrenRecursively(roots);

    return roots;
  }

  /**
   * 카테고리 정보를 조회합니다.
   *
   * @param id 카테고리 ID
   * @return 카테고리 정보
   * @throws NotFoundException 카테고리가 존재하지 않을 경우
   */
  public CategoryResponse getCategory(Long id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(CategoryConstants.NOT_FOUND));
    return CategoryMapper.toResponse(category);
  }

  /**
   * 모든 카테고리의 평면 리스트를 조회합니다.
   *
   * @return 카테고리 리스트
   */
  public List<CategoryResponse> getCategoryFlatList() {
    return categoryRepository.findAll().stream()
        .map(CategoryMapper::toResponse)
        .toList();
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
    else if (parentId.equals(category.getId())) {
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

  private void sortChildrenRecursively(List<CategoryTreeResponse> list) {
    if (list == null) return;
    list.sort(Comparator.comparing(CategoryTreeResponse::sortOrder).reversed());
    list.forEach(node -> sortChildrenRecursively(node.children()));
  }
}
