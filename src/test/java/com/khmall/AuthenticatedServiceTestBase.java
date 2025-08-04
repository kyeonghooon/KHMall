package com.khmall;

import com.khmall.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class AuthenticatedServiceTestBase {

  protected Long ADMIN_ID = 1L;
  protected String ADMIN_NAME = "admin";
  protected String ADMIN_PASS = "password";

  @BeforeEach
  void setUpAuthContext() {
    CustomUserDetails userDetails = new CustomUserDetails(
        ADMIN_ID, ADMIN_NAME, ADMIN_PASS, "ADMIN"
    );
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void clearAuthContext() {
    SecurityContextHolder.clearContext();
  }
}
