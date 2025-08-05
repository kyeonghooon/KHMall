package com.khmall.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.khmall.domain.user.dto.SignupRequest;
import com.khmall.support.TestBase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SignupRequestTest extends TestBase {

  static Validator validator;

  @BeforeAll
  static void setUpValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void 아이디_비어있으면_에러() {
    SignupRequest dto = new SignupRequest("", "12345678", "홍길동");
    Set<ConstraintViolation<SignupRequest>> violations = validator.validate(dto);

    assertThat(violations).anyMatch(v ->
        v.getPropertyPath().toString().equals("username") &&
            v.getMessage().equals("아이디는 필수 입력입니다.")
    );
  }

  @Test
  void 비밀번호_길이_에러() {
    SignupRequest dto = new SignupRequest("user1", "123", "홍길동");
    Set<ConstraintViolation<SignupRequest>> violations = validator.validate(dto);

    assertThat(violations).anyMatch(v ->
        v.getPropertyPath().toString().equals("password") &&
            v.getMessage().equals("비밀번호는 4~20자여야 합니다.")
    );
  }
}

