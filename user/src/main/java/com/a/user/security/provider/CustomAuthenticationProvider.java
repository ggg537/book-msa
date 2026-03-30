package com.a.user.security.provider;

import com.a.user.security.details.CustomUserDetails;
import com.a.user.security.details.CustomUserDetailsService;
import com.a.user.security.token.CustomAuthenticationToken;
import com.a.user.security.util.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 실제 인증 로직 처리 클래스
 *
 * 역할:
 *   CustomAuthenticationFilter 가 던진 인증 요청을 받아서
 *   DB 조회 → 계정 상태 확인 → 비밀번호 검증 → 인증 완료 처리
 *
 * 흐름:
 *   CustomAuthenticationFilter
 *   → unauthenticated(email, password) 생성
 *   → AuthenticationManager 에게 전달
 *   → CustomAuthenticationProvider.authenticate() 호출  ← 여기
 *   → 성공: authenticated 토큰 반환 → SuccessHandler
 *   → 실패: AuthenticationException 발생 → FailureHandler
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 인증 처리 핵심 메서드
     *
     * @param authentication CustomAuthenticationFilter 가 생성한
     *                       unauthenticated 토큰 (email, password 포함)
     * @return 인증 성공 시 authenticated 토큰 반환
     *         → CustomAuthenticationSuccessHandler 로 전달
     * @throws AuthenticationException 인증 실패 시
     *         → CustomAuthenticationFailureHandler 로 전달
     */
    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        // 로그인 요청 IP 추출 (보안 감사 로그용)
        String clientIp = getClientIp();

        // unauthenticated 토큰에서 email, password 꺼냄
        String email    = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        // ① DB에서 유저 조회
        // 없으면 BusinessException(MEMBER_NOT_FOUND) 발생
        // → CustomAuthenticationFailureHandler 로 전달
        CustomUserDetails userDetails =
                (CustomUserDetails) userDetailsService.loadUserByUsername(email);

        // ② 계정 활성화 여부 확인 (탈퇴/정지 회원)
        // isEnabled() = false 이면 DisabledException 발생
        if (!userDetails.isEnabled()) {
            log.warn("비활성화된 계정 로그인 시도 - email: {}, ip: {}", email, clientIp);
            throw new DisabledException("비활성화된 계정입니다.");
        }

        // ③ 계정 잠금 여부 확인
        // isAccountNonLocked() = false 이면 LockedException 발생
        if (!userDetails.isAccountNonLocked()) {
            log.warn("잠긴 계정 로그인 시도 - email: {}, ip: {}", email, clientIp);
            throw new LockedException("잠긴 계정입니다.");
        }

        // ④ 비밀번호 검증
        // BCrypt 해시 비교
        // 불일치 시 BadCredentialsException 발생
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            log.warn("비밀번호 불일치 - email: {}, ip: {}", email, clientIp);
            throw new BadCredentialsException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        log.info("로그인 성공 - email: {}, ip: {}", email, clientIp);

        // ⑤ 인증 성공 → authenticated 토큰 생성 후 반환
        // userId, email 포함 → SecurityContext 에 저장
        // → CustomAuthenticationSuccessHandler 에서 JWT 발급
        return CustomAuthenticationToken.authenticated(
                userDetails,
                userDetails.getUserId(),
                userDetails.getEmail(),
                userDetails.getAuthorities()
        );
    }

    /**
     * 이 Provider 가 처리할 수 있는 토큰 타입 지정
     * CustomAuthenticationToken 타입만 처리
     * 다른 타입의 토큰은 다른 Provider 가 처리
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return CustomAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * 현재 요청의 클라이언트 IP 추출
     * 로그인 성공/실패 보안 감사 로그에 사용
     * WebUtil.getClientIp() 에 위임
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder
                            .currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            return WebUtil.getClientIp(request);
        } catch (Exception e) {
            return "unknown";
        }
    }
}
