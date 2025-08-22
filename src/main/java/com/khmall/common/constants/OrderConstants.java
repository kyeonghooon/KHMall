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
  public static final String NOT_FOUND = "주문을 찾을 수 없습니다.";
  public static final String DRAFT_NOT_FOUND = "주문 항목이 비어있습니다.";
  public static final String NOT_PAY_WAIT = "결제 대기 상태가 아닙니다.";
  public static final String NOT_PAID = "결제가 완료되지 않았습니다.";
  public static final String NOT_DELIVERY = "배송중인 주문이 아닙니다.";
  public static final String NOT_ORDER_OWNER = "주문 소유자가 아닙니다.";
  public static final String ALREADY_PAID = "이미 결제가 완료된 주문입니다.";
}
