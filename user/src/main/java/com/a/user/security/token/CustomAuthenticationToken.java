package com.a.user.security.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Spring Security 커스텀 인증 객체
 * UsernamePasswordAuthenticationToken 대신 사용
 *
 * 사용처:
 *   CustomAuthenticationFilter  → 로그인 요청 시 unauthenticated() 생성
 *   CustomAuthenticationProvider → 인증 성공 시 authenticated() 생성
 *   JwtAuthenticationFilter     → 토큰 검증 후 authenticated() 생성
 *   SecurityContext             → 인증 정보 저장 후 어디서든 꺼내 사용
 */
public class CustomAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;   // 인증 전: email / 인증 후: CustomUserDetails
    private Object credentials;       // 인증 전: password / 인증 후: null (보안상 제거)
    private final Long userId;        // 인증 후 userId (API 에서 꺼내 사용)
    private final String email;       // 인증 후 email (API 에서 꺼내 사용)

    // =========================================================================
    // 인증 전 토큰 (로그인 요청 시)
    // CustomAuthenticationFilter 에서 생성
    // email, password 만 담음
    // =========================================================================
    public static CustomAuthenticationToken unauthenticated(String email, String password) {
        return new CustomAuthenticationToken(email, password);
    }

    // =========================================================================
    // 인증 후 토큰 (Provider 검증 완료 후)
    // CustomAuthenticationProvider, JwtAuthenticationFilter 에서 생성
    // userId, email, authorities 포함
    // credentials(비밀번호) 는 보안상 null 처리
    // =========================================================================
    public static CustomAuthenticationToken authenticated(
            Object principal,
            Long userId,
            String email,
            Collection<? extends GrantedAuthority> authorities
    ) {
        return new CustomAuthenticationToken(principal, userId, email, authorities);
    }

    // 인증 전 생성자
    private CustomAuthenticationToken(Object principal, Object credentials) {
        super(null);
        this.principal   = principal;
        this.credentials = credentials;
        this.userId      = null;
        this.email       = null;
        setAuthenticated(false);
    }

    // 인증 후 생성자
    private CustomAuthenticationToken(
            Object principal,
            Long userId,
            String email,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.principal   = principal;
        this.userId      = userId;
        this.email       = email;
        this.credentials = null;        // 인증 완료 후 비밀번호 메모리에서 제거
        super.setAuthenticated(true);
    }

    @Override
    public Object getPrincipal() { return principal; }

    @Override
    public Object getCredentials() { return credentials; }

    // =========================================================================
    // 커스텀 getter
    // SecurityContext 에서 꺼낸 후 사용
    //
    // 예시:
    //   CustomAuthenticationToken token =
    //       (CustomAuthenticationToken) SecurityContextHolder
    //           .getContext().getAuthentication();
    //   Long userId = token.getUserId();
    // =========================================================================
    public Long getUserId() { return userId; }

    public String getEmail() { return email; }
}
