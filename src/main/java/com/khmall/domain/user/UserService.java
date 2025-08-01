package com.khmall.domain.user;

import com.khmall.domain.user.dto.LoginRequest;
import com.khmall.domain.user.dto.SignupRequest;
import com.khmall.domain.user.dto.UserResponse;
import com.khmall.exception.custom.DuplicateException;
import com.khmall.exception.custom.NotFoundException;
import com.khmall.exception.custom.UnauthenticatedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * 회원 가입을 처리합니다.
   *
   * @param request 회원 가입 요청 정보
   * @return 가입된 사용자 정보
   * @throws DuplicateException 이미 사용 중인 아이디일 경우
   */
  @Transactional
  public UserResponse signup(SignupRequest request) {
    if (userRepository.existsByUsername(request.username())) {
      throw new DuplicateException("이미 사용 중인 아이디입니다.");
    }
    String encodedPassword = passwordEncoder.encode(request.password());
    User user = UserMapper.toEntity(request, encodedPassword);
    User saved = userRepository.save(user);
    return UserMapper.toResponse(saved);
  }

  /**
   * 로그인 처리를 합니다.
   *
   * @param request 로그인 요청 정보
   * @return 로그인된 사용자 정보
   * @throws NotFoundException 사용자 정보가 존재하지 않을 경우
   * @throws UnauthenticatedException 비밀번호가 일치하지 않을 경우
   */
  public UserResponse login(LoginRequest request) {
    User user = userRepository.findByUsername(request.username())
        .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new UnauthenticatedException("비밀번호가 일치하지 않습니다.");
    }

    return UserMapper.toResponse(user);
  }
}
