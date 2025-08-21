package com.khmall.common.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryConstants {

  // error messages
  public static final String NOT_FOUND = "재고를 찾을 수 없습니다.";
  public static final String QUANTITY_POSITIVE_MESSAGE = "수량은 1 이상이어야 합니다.";
  public static final String QUANTITY_NON_NEGATIVE_MESSAGE = "수량은 음수일 수 없습니다.";
  public static final String INSUFFICIENT_STOCK_MESSAGE = "재고 부족: 요청=%d, 보유=%d";

  // log reasons
  public static final String REASON_INCREASE = "입고";
  public static final String REASON_SELL = "판매 차감";
  public static final String REASON_RETURN = "반품 입고";
  public static final String REASON_OVERWRITE = "재고 수정";
  public static final String REASON_INITIAL = "초기 재고 설정";

}
