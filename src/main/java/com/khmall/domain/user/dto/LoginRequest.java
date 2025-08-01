package com.khmall.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "아이디는 필수 입력입니다.")
    String username,

    @NotBlank(message = "비밀번호는 필수 입력입니다.")
    @Size(min = 4, max = 20, message = "비밀번호는 4~20자여야 합니다.")
    String password) {

}
