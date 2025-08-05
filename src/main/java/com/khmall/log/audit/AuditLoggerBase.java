package com.khmall.log.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;

@RequiredArgsConstructor
public abstract class AuditLoggerBase {

  protected final AuditorAware<Long> auditorAware;

  protected Long getCurrentUserId() {
    return auditorAware.getCurrentAuditor().orElse(null);
  }
}
