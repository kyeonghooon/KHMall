package com.khmall.domain.category;

import static com.khmall.common.constants.CategoryConstants.DEPTH_EXCEEDED;
import static com.khmall.common.constants.CategoryConstants.DEPTH_MAX;
import static com.khmall.common.constants.CategoryConstants.NAME_DUPLICATE;

import com.khmall.exception.custom.BadRequestException;
import com.khmall.exception.custom.DuplicateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryValidator {
  private final CategoryRepository categoryRepository;

  // 깊이 제한 검증
  public void validateDepth(Category parent) {
    int depth = 1;
    Category current = parent;
    while (current != null) {
      current = current.getParent();
      depth++;
      if (depth > DEPTH_MAX) {
        throw new BadRequestException(DEPTH_EXCEEDED);
      }
    }
  }

  // 이름 중복 검증
  public void validateDuplicateName(Category parent, String name) {
    if (categoryRepository.existsByParentAndName(parent, name)) {
      throw new DuplicateException(NAME_DUPLICATE);
    }
  }
}
