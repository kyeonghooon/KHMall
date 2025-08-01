package com.khmall.exception;

import com.khmall.exception.ErrorResponse.FieldErrorDetail;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 모든 커스텀 예외를 처리하는 핸들러입니다.
   *
   * @param ex 발생한 예외
   * @return 에러 응답
   */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
    return ResponseEntity
        .status(ex.getStatus())
        .body(new ErrorResponse(ex.getStatus().value(), ex.getMessage(), null));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
    List<FieldErrorDetail> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(fe -> new ErrorResponse.FieldErrorDetail(fe.getField(), fe.getDefaultMessage()))
        .toList();

    ErrorResponse body = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        "입력값이 올바르지 않습니다.",
        errors
    );
    return ResponseEntity.badRequest().body(body);
  }
}
