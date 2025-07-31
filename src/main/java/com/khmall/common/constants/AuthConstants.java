package com.khmall.common.constants;

public final class AuthConstants {

  private AuthConstants() {
    // 인스턴스화 방지
  }

  public static final String AUTH_HEADER = "Authorization";
  public static final String TOKEN_PREFIX = "Bearer ";
  public static final int TOKEN_PREFIX_LENGTH = TOKEN_PREFIX.length();
}
