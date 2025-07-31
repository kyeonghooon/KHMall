package com.khmall.domain.user.dto;

import com.khmall.domain.user.Role;

public record UserResponse(
    Long userId,
    String username,
    String name,
    Role role
) {

}
