package com.khmall.user;

import com.khmall.security.JwtProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JwtProviderTest {
  private final String secret = "testtesttesttesttesttesttesttest1234";
  private final JwtProvider jwtProvider = new JwtProvider(secret);

  @Test
  void 토큰_정상생성_파싱_성공() {
    // Given
    String username = "tester";
    String role = "ADMIN";

    // When
    String token = jwtProvider.createToken(username, role);

    // Then
    assertTrue(jwtProvider.validateToken(token));
    assertEquals(username, jwtProvider.getUsername(token));
    assertEquals(role, jwtProvider.getRole(token));
  }

  @Test
  void 시크릿키가_다르면_토큰_검증_실패() {
    // Given
    String secretB = "differentdifferentdifferentdifferent9876";
    JwtProvider jwtProviderA = new JwtProvider(secret);
    JwtProvider jwtProviderB = new JwtProvider(secretB);

    String token = jwtProviderA.createToken("tester", "ADMIN");

    // When & Then
    assertFalse(jwtProviderB.validateToken(token));
    assertThrows(Exception.class, () -> jwtProviderB.getUsername(token));
    assertThrows(Exception.class, () -> jwtProviderB.getRole(token));
  }

  @Test
  void 만료된_토큰_검증_실패() throws InterruptedException {
    // Given
    String secret = "testtesttesttesttesttesttesttest1234";
    JwtProvider jwtProvider = new JwtProvider(secret);

    // 토큰 만료시간을 매우 짧게 설정해서 생성
    String token = Jwts.builder()
        .subject("tester")
        .claim("role", "ADMIN")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + 100)) // 0.1초 후 만료
        .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
        .compact();

    Thread.sleep(200); // 토큰이 만료되길 기다림

    // When & Then
    assertFalse(jwtProvider.validateToken(token));
    assertThrows(Exception.class, () -> jwtProvider.getUsername(token));
  }
}
