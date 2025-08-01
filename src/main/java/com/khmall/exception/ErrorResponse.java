package com.khmall.exception;

import java.util.List;

public record ErrorResponse(
    int status,
    String message, // 전체 에러 메시지
    List<FieldErrorDetail> errors // 필드별 에러 메시지
) {

  public record FieldErrorDetail(String field, String message) {

  }
}
