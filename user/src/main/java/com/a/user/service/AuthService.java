package com.a.user.service;

import com.a.user.domain.Member;
import com.a.user.domain.Role;
import com.a.user.dto.request.SignupRequest;
import com.a.user.dto.response.MemberResponse;
import com.a.user.exception.BusinessException;
import com.a.user.exception.errorCode.ErrorCode;
import com.a.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원가입 비즈니스 로직 서비스
 *
 * 역할:
 *   회원가입, 이메일/닉네임 중복 확인
 *
 * 로그인/로그아웃은 Spring Security 가 처리
 *   → CustomAuthenticationFilter → Provider → SuccessHandler
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder  passwordEncoder;

    /**
     * 회원가입
     * 이메일/닉네임 중복 확인 → BCrypt 암호화 → 저장
     */
    @Transactional
    public MemberResponse signup(SignupRequest request) {

        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.MEMBER_DUPLICATE_EMAIL);
        }

        if (memberRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.MEMBER_DUPLICATE_NICKNAME);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        Member member = Member.of(
                request.email(),
                encodedPassword,
                request.name(),
                request.nickname(),
                request.phone(),
                Role.USER
        );

        Member saved = memberRepository.save(member);
        log.info("회원가입 완료 - email: {}", saved.getEmail());

        return MemberResponse.from(saved);
    }

    /**
     * 이메일 중복 확인
     * @return true = 사용 가능 / false = 중복
     */
    @Transactional(readOnly = true)
    public boolean checkEmail(String email) {
        return !memberRepository.existsByEmail(email);
    }

    /**
     * 닉네임 중복 확인
     * @return true = 사용 가능 / false = 중복
     */
    @Transactional(readOnly = true)
    public boolean checkNickname(String nickname) {
        return !memberRepository.existsByNickname(nickname);
    }
}
