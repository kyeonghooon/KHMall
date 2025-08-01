package com.khmall.security;

import com.khmall.common.constants.AuthConstants;
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

  public JwtProvider(@Value("${jwt.secret}") String secret) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  // JWT 토큰 생성
  public String createToken(String username, String role) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + AuthConstants.TOKEN_EXPIRATION_TIME);

    return Jwts.builder()
        .subject(username)
        .claim(ROLE_CLAIM, role)
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

  // 사용자명 추출
  public String getUsername(String token) {
    return getClaims(token).getSubject();
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
