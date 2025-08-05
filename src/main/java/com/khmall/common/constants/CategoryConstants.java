package com.khmall.common.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryConstants {

  // Category properties
  public static final int NAME_MAX_LENGTH = 20;
  public static final int SORT_ORDER_MIN = 0;
  public static final int DEPTH_MAX = 2;

  // Validation messages
  public static final String NAME_NOT_BLANK_MESSAGE = "카테고리명을 입력해주세요.";
  public static final String NAME_SIZE_MESSAGE = "카테고리명은 최대 " + NAME_MAX_LENGTH + "자까지 입력 가능합니다.";
  public static final String SORT_ORDER_MIN_MESSAGE = "정렬 순서는 " + SORT_ORDER_MIN + "이상이어야 합니다.";
  public static final String SELF_REFERENCE_MESSAGE = "상위 카테고리는 자기 자신을 참조할 수 없습니다.";

  // Error messages
  public static final String PARENT_NOT_FOUND = "상위 카테고리가 존재하지 않습니다.";
  public static final String NOT_FOUND = "카테고리가 존재하지 않습니다.";
  public static final String NAME_DUPLICATE = "이미 존재하는 이름의 카테고리입니다.";
  public static final String DEPTH_EXCEEDED = "카테고리의 최대 깊이를 초과했습니다. (최대 " + DEPTH_MAX + "단계)";
}
