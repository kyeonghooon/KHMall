package com.khmall.user;

import com.khmall.domain.user.Role;
import com.khmall.domain.user.UserRepository;
import com.khmall.domain.user.UserService;
import com.khmall.domain.user.dto.LoginRequest;
import com.khmall.domain.user.dto.SignupRequest;
import com.khmall.domain.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

  @Autowired
  UserService userService;

  @Autowired
  UserRepository userRepository;

  @Test
  void 회원가입_성공() {
    // Given: 회원가입 요청 DTO 준비
    SignupRequest request = new SignupRequest(
        "tester",
        "password123",
        "홍길동"
    );
    // When: 회원가입 수행
    UserResponse response = userService.signup(request);

    // Then: 응답 검증
    assertEquals("tester", response.username());
    assertEquals("홍길동", response.name());
    assertEquals(Role.USER, response.role());
  }

  @Test
  void 로그인_성공() {
    // Given: 사전에 회원가입을 수행
    userService.signup(new SignupRequest("loginuser", "pw", "이름"));

    // When: 로그인 요청
    LoginRequest req = new LoginRequest("loginuser", "pw");
    UserResponse resp = userService.login(req);

    // Then: 결과 검증
    assertEquals("loginuser", resp.username());
  }

  @Test
  void 아이디_중복_에러() {
    // Given: 이미 존재하는 아이디로 회원가입
    userService.signup(new SignupRequest("dup", "pw", "a"));

    // When & Then: 같은 아이디로 다시 가입 시도 → 예외 발생
    SignupRequest req = new SignupRequest("dup", "pw", "b");
    assertThrows(IllegalArgumentException.class, () ->
        userService.signup(req)
    );
  }

  @Test
  void 비밀번호_불일치_에러() {
    // Given: 회원가입 완료
    userService.signup(new SignupRequest("pwtest", "pw1", "a"));

    // When & Then: 잘못된 비밀번호로 로그인 시도 → 예외 발생
    LoginRequest req = new LoginRequest("pwtest", "wrongpw");
    assertThrows(IllegalArgumentException.class, () ->
        userService.login(req)
    );
  }
}
