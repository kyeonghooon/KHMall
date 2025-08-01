package com.khmall.common.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthConstants {

  public static final String AUTH_HEADER = "Authorization";
  public static final String TOKEN_PREFIX = "Bearer ";
  public static final int TOKEN_PREFIX_LENGTH = TOKEN_PREFIX.length();
  public static final long TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24L; // 24h
}
