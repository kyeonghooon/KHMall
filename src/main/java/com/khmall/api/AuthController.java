package com.khmall.api;

import com.khmall.domain.user.UserService;
import com.khmall.domain.user.dto.LoginRequest;
import com.khmall.domain.user.dto.SignupRequest;
import com.khmall.domain.user.dto.TokenResponse;
import com.khmall.domain.user.dto.UserResponse;
import com.khmall.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserService userService;
  private final JwtProvider jwtProvider;

  @PostMapping("/signup")
  public ResponseEntity<UserResponse> signup(@RequestBody SignupRequest req) {
    UserResponse user = userService.signup(req);
    return ResponseEntity.ok(user);
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {
    UserResponse user = userService.login(req);
    System.out.println("User logged in: " + user.username());
    String token = jwtProvider.createToken(user.username(), user.role().name());
    return ResponseEntity.ok(new TokenResponse(token));
  }
}
