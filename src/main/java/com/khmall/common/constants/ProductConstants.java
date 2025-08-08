package com.khmall.common.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductConstants {

  // Product properties
  public static final int NAME_MAX_LENGTH = 150;

  // Validation messages
  public static final String CATEGORY_NOT_BLANK_MESSAGE = "상품 카테고리를 선택해주세요.";
  public static final String NAME_NOT_BLANK_MESSAGE = "상품명을 입력해주세요.";
  public static final String NAME_SIZE_MESSAGE = "상품명은 최대 " + NAME_MAX_LENGTH + "자까지 입력 가능합니다.";
  public static final String PRICE_NOT_BLANK_MESSAGE = "상품 가격을 입력해주세요.";
  public static final String PRICE_MIN_MESSAGE = "상품 가격은 0 이상이어야 합니다.";
  public static final String IMAGE_KEY_NOT_BLANK_MESSAGE = "상품 이미지를 업로드해주세요.";
  public static final String STATUS_NOT_BLANK_MESSAGE = "상품 판매 상태를 선택해주세요.";
  public static final String QUANTITY_NOT_BLANK_MESSAGE = "상품 수량을 입력해주세요.";
  public static final String QUANTITY_MIN_MESSAGE = "상품 수량은 0 이상이어야 합니다.";

  // Error messages

}
