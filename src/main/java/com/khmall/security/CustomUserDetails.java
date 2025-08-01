package com.khmall.security;

import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetails implements UserDetails {

  private final Long userId;
  private final String username;
  private final String password;
  private final String role;
  private final Collection<? extends GrantedAuthority> authorities;

  public CustomUserDetails(Long userId, String username, String password, String role) {
    this.userId = userId;
    this.username = username;
    this.password = password;
    this.role = role;
    this.authorities = List.of(() -> "ROLE_" + role);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  // 계정 만료/잠김/패스워드 만료/활성화 관련 메서드
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
