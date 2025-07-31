package com.khmall.domain.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  // 아이디 중복 체크를 위한 메소드
  boolean existsByUsername(String username);

  // 로그인 시 사용할 메소드
  Optional<User> findByUsername(String username);

}