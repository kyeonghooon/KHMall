package com.khmall.log.audit;

import com.khmall.domain.category.dto.CategoryResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class CategoryLogger extends AuditLoggerBase {

  public CategoryLogger(AuditorAware<Long> auditorAware) {
    super(auditorAware);
  }

  @Pointcut("execution(* com.khmall.domain.category.CategoryService.createCategory(..))")
  private void categoryCreate() {}

  @Pointcut("execution(* com.khmall.domain.category.CategoryService.updateCategory(..))")
  private void categoryUpdate() {}

//  @Pointcut("execution(* com.khmall.domain.category.CategoryService.deleteCategory(..))")
//  private void categoryDelete() {}

  @Pointcut("categoryCreate() || categoryUpdate()")
  private void categoryCU() {}

  @AfterReturning(pointcut = "categoryCU()", returning = "result")
  public void logCategoryUpdate(JoinPoint joinPoint, Object result) {
    if (!(result instanceof CategoryResponse response)) return;

    Long userId = getCurrentUserId();
    String methodName = joinPoint.getSignature().getName();
    String action = switch (methodName) {
      case "createCategory" -> "생성";
      case "updateCategory" -> "수정";
      default -> methodName;
    };

    try {
      if (userId != null) MDC.put("userId", userId.toString());
      if (response.categoryId() != null) MDC.put("categoryId", response.categoryId().toString());

      log.info("카테고리 {}: name='{}', parentId={}, sortOrder={}",
          action, response.name(), response.parentId(), response.sortOrder());

    } finally {
      MDC.clear();
    }
  }
}
