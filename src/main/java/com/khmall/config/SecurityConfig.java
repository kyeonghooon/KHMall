package com.khmall.config;

import com.khmall.security.JwtAuthenticationFilter;
import com.khmall.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtProvider jwtProvider;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // CSRF 보호 비활성화
        .csrf(AbstractHttpConfigurer::disable)

        // 세션을 완전히 사용하지 않도록(STATELESS) 설정
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // URL 인증/인가 설정
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                // /api/auth/** 경로(회원가입, 로그인)는 인증 없이 접근 허용
                "/api/auth/**",
                // Swagger 관련 경로 인증 없이 접근 허용
                "/v3/api-docs/**",
                "/swagger-ui/**"
            ).permitAll()
            // 나머지 모든 요청은 인증 필요(JWT 없으면 401)
            .anyRequest().authenticated())

        // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 추가 (실제 인증은 JWT로만)
        .addFilterBefore(
            new JwtAuthenticationFilter(jwtProvider),
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
