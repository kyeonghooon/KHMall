package com.khmall.util;

import com.khmall.domain.user.Role;
import com.khmall.domain.user.User;
import com.khmall.domain.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TestEntityFactory {

  public static User createAdmin(UserRepository userRepository, PasswordEncoder encoder, String username,
      String password, String name) {
    User admin = User.builder()
        .username(username)
        .password(encoder.encode(password))
        .name(name)
        .role(Role.ADMIN)
        .build();
    return userRepository.save(admin);
  }
}
