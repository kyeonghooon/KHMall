package com.khmall.common.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryConstants {
  public static final int NAME_MAX_LENGTH = 20;
  public static final int SORT_ORDER_MIN = 0;

  public static final String CATEGORY_NOT_FOUND = "상위 카테고리가 존재하지 않습니다.";
  public static final String CATEGORY_NAME_DUPLICATE = "이미 존재하는 이름의 카테고리입니다.";
}
