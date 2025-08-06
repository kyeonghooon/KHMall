package com.khmall.log.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class AuditLogger {

  private final AuditorAware<Long> auditorAware;

  @AfterReturning(
      pointcut =
          "execution(public * com.khmall..*Service.create*(..)) || " +
              "execution(public * com.khmall..*Service.update*(..)) || " +
              "execution(public * com.khmall..*Service.delete*(..))",
      returning = "result"
  )
  public void logChange(JoinPoint joinPoint, Object result) {
    Long userId = auditorAware.getCurrentAuditor().orElse(null);

    // 도메인 추출: Service 클래스 이름에서 "Service"를 제거
    String domain = joinPoint.getTarget().getClass().getSimpleName().replace("Service", "");

    // 동작 추출: create/update/delete만 남김
    String methodName = joinPoint.getSignature().getName();
    String opKey = methodName.length() >= 6 ? methodName.substring(0, 6) : methodName;
    String action = switch (opKey) {
      case "create" -> "생성";
      case "update" -> "수정";
      case "delete" -> "삭제";
      default -> methodName;
    };

    try {
      if (userId != null) {
        MDC.put("userId", userId.toString());
      }
      log.info("{} {} : {}", domain, action, result);
    } finally {
      MDC.clear();
    }
  }
}
