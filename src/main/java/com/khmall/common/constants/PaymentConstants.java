package com.khmall.common.constants;

public class PaymentConstants {

  // validation messages
  public static final String AMOUNT_NOT_BLANK_MESSAGE = "결제 금액은 필수입니다.";
  public static final String AMOUNT_POSITIVE_MESSAGE = "결제 금액은 양수여야 합니다.";
  public static final String PAYMENT_METHOD_NOT_NULL_MESSAGE = "결제 수단은 필수입니다.";

  // error messages
  public static final String NOT_FOUND = "결제를 찾을 수 없습니다.";
  public static final String NOT_READY = "결제 준비가 아닙니다.";
  public static final String AMOUNT_MISMATCH = "결제 금액이 일치하지 않습니다.";
  public static final String PAID_NOT_FOUND = "완료된 결제를 찾을 수 없습니다.";

}
