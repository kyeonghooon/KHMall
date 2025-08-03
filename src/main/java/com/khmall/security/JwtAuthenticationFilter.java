package com.khmall.security;

import com.khmall.common.constants.AuthConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtProvider jwtProvider;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String header = request.getHeader(AuthConstants.AUTH_HEADER);
    if (header != null && header.startsWith(AuthConstants.TOKEN_PREFIX)) {
      String token = header.substring(AuthConstants.TOKEN_PREFIX_LENGTH);

      if (jwtProvider.validateToken(token)) {
        String username = jwtProvider.getUsername(token);
        String role = jwtProvider.getRole(token);
        Long userId = jwtProvider.getUserId(token);

        CustomUserDetails userDetails = new CustomUserDetails(userId, username, null, role);

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
            );
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }
    filterChain.doFilter(request, response);
  }
}
