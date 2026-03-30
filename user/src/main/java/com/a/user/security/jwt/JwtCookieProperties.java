package com.a.user.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 쿠키 설정값
 * application.yml jwt.cookie.* 값 바인딩
 *
 * 사용처:
 *   JwtCookieWriter  → 쿠키 저장 시 설정값 사용
 *   JwtCookieResolver → 쿠키 이름으로 토큰 추출
 */
@ConfigurationProperties(prefix = "jwt.cookie")
public record JwtCookieProperties(

        /** AccessToken 쿠키 이름 (기본: accessToken) */
        String accessTokenName,

        /** RefreshToken 쿠키 이름 (기본: refreshToken) */
        String refreshTokenName,

        /** AccessToken 쿠키 유효시간 (초) - 기본 1800 = 30분 */
        int accessTokenMaxAge,

        /** RefreshToken 쿠키 유효시간 (초) - 기본 604800 = 7일 */
        int refreshTokenMaxAge,

        /** 쿠키 도메인 (로컬: localhost, 운영: example.com) */
        String domain,

        /** 쿠키 경로 (기본: /) */
        String path,

        /** HttpOnly 여부 - XSS 방어 */
        boolean httpOnly,

        /** Secure 여부 - HTTPS 전용 (로컬: false, 운영: true) */
        boolean secure,

        /** SameSite 설정 - CSRF 방어 (Strict / Lax / None) */
        String sameSite
) {}
