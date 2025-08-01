package com.khmall.config;

import com.khmall.security.CustomUserDetails;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditorAwareImpl implements AuditorAware<Long> {

  @Override
  public Optional<Long> getCurrentAuditor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // 인증 안됐으면 null 반환 (Spring Data JPA에서 알아서 처리)
    if (authentication == null || !authentication.isAuthenticated()) {
      return Optional.empty();
    }

    // principal에서 userId 추출
    Object principal = authentication.getPrincipal();
    if (principal instanceof CustomUserDetails userDetails) {
      return Optional.of(userDetails.getUserId());
    }

    return Optional.empty();
  }
}
