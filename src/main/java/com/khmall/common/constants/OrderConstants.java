package com.khmall.common.constants;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class OrderConstants {

  // validation messages
  public static final String CART_ITEM_NOT_EMPTY = "선택한 장바구니 항목이 없습니다.";
  public static final String PRODUCT_NOT_NULL = "상품을 선택 해주세요.";
  public static final String QUANTITY_NOT_NULL = "수량을 선택 해주세요.";
  public static final String QUANTITY_MIN = "수량은 1개 이상이어야 합니다.";

  // error messages
  public static final String DRAFT_NOT_FOUND = "주문 항목이 비어있습니다.";
}
