package com.a.user.security.path;

/**
 * 인증 없이 접근 가능한 경로 목록
 *
 * 사용처:
 *   SecurityConfig → .requestMatchers(PERMIT_ALL_PATHS).permitAll()
 */
public class PermitAllPath {

    public static final String[] PERMIT_ALL_PATHS = {
            // 루트 및 페이지
            "/",
            "/main",
            "/login",
            "/signup",
            "/error",
            "/favicon.ico",

            // 정적 리소스
            "/css/**",
            "/js/**",
            "/images/**",

            // 인증 API
            "/api/auth/login",
            "/api/auth/signup",
            "/api/auth/reissue",
            "/api/auth/check-email",
            "/api/auth/check-nickname"
    };

    // 인스턴스 생성 방지
    private PermitAllPath() {}
}
