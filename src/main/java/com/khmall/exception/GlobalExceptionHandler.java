package com.khmall.exception;

import com.khmall.exception.ErrorResponse.FieldErrorDetail;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
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

  /**
   * 유효성 검사시 발생하는 예외를 처리하는 핸들러입니다.
   *
   * @param ex 발생한 예외
   * @return 에러 응답
   */
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

  /**
   * 인증되지 않은 접근을 처리하는 핸들러입니다.
   *
   * @param ex 발생한 예외
   * @return 에러 응답
   */
  @ExceptionHandler(AuthorizationDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
    log.warn("권한이 없는 접근 시도: {}", ex.getMessage());

    ErrorResponse body = new ErrorResponse(
        HttpStatus.FORBIDDEN.value(),
        "접근 권한이 없습니다.",
        null
    );
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
  }

  /**
   * 그 외 모든 예외를 처리하는 핸들러입니다.
   *
   * @param ex 발생한 예외
   * @return 에러 응답
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {

    // 로그에 예외 정보 출력
    log.error("알 수 없는 에러 발생", ex);

    // 일반 예외는 500 에러로 처리
    ErrorResponse body = new ErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "서버 오류가 발생했습니다.",
        null
    );
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }
}
