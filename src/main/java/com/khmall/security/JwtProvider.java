package com.khmall.security;

import com.khmall.common.constants.AuthConstants;
import com.khmall.domain.user.dto.UserResponse;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

  private final SecretKey secretKey;

  private static final String ROLE_CLAIM = "role";
  private static final String USER_ID_CLAIM = "userId";

  public JwtProvider(@Value("${jwt.secret}") String secret) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  // JWT 토큰 생성
  public String createToken(UserResponse user) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + AuthConstants.TOKEN_EXPIRATION_TIME);

    return Jwts.builder()
        .subject(user.username())
        .claim(USER_ID_CLAIM, user.userId())
        .claim(ROLE_CLAIM, user.role().name())
        .issuedAt(now)
        .expiration(validity)
        .signWith(secretKey)
        .compact();
  }

  // 토큰에서 Payload 추출
  private Claims getClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  // 사용자아이디 (로그인) 추출
  public String getUsername(String token) {
    return getClaims(token).getSubject();
  }

  // 사용자id 추출
  public Long getUserId(String token) {
    return getClaims(token).get(USER_ID_CLAIM, Long.class);
  }

  // 역할(role) 추출
  public String getRole(String token) {
    return getClaims(token).get(ROLE_CLAIM, String.class);
  }

  // 토큰 유효성 검사
  public boolean validateToken(String token) {
    try {
      getClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }
}
