package com.khmall.common.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductConstants {

  // imageKey pattern
  public static final String IMAGE_KEY_PREFIX = "products/";
  public static final String IMAGE_KEY_PATTERN = "^"+ IMAGE_KEY_PREFIX +"\\d{4}/(0[1-9]|1[0-2])/[a-f0-9]{32}\\.(jpg|jpeg|png|webp)$";

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
  public static final String IMAGE_KEY_PATTERN_MESSAGE = "상품 이미지 키 형식이 올바르지 않습니다. (예: products/2023/01/uuid.jpg)";

  // Error messages

}
