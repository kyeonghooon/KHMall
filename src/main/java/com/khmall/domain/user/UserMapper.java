package com.khmall.domain.user;

import com.khmall.domain.user.dto.SignupRequest;
import com.khmall.domain.user.dto.UserResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

  public static UserResponse toResponse(User user) {
    return new UserResponse(
        user.getId(),
        user.getUsername(),
        user.getName(),
        user.getRole()
    );
  }

  public static User toEntity(SignupRequest request, String encodedPassword) {
    return User.builder()
        .username(request.username())
        .password(encodedPassword)
        .name(request.name())
        .role(Role.USER) // 기본 역할은 USER로 설정
        .build();
  }

}
