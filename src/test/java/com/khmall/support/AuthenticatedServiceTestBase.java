package com.khmall.support;

import com.khmall.domain.user.User;
import com.khmall.domain.user.UserRepository;
import com.khmall.security.CustomUserDetails;
import com.khmall.util.TestEntityFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

public abstract class AuthenticatedServiceTestBase extends TestBase {

  @Autowired
  protected UserRepository userRepository;

  @Autowired
  protected PasswordEncoder passwordEncoder;

  protected Long adminId = 1L;
  protected final String ADMIN_USERNAME = "admin";
  protected final String ADMIN_PASS = "password";
  protected final String ADMIN_NAME = "관리자";

  @BeforeEach
  void setUpAuthContext() {
    // DB에 관리자가 없으면 새로 생성
    User admin = userRepository.findById(adminId)
        .orElseGet(
            () -> TestEntityFactory.createAdmin(userRepository, passwordEncoder, ADMIN_USERNAME, ADMIN_PASS,
                ADMIN_NAME));
    adminId = admin.getUserId();
    CustomUserDetails userDetails = new CustomUserDetails(
        adminId, admin.getUsername(), admin.getPassword(), admin.getRole().name()
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
