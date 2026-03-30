package com.a.user.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰을 쿠키에 저장/삭제하는 전담 클래스
 *
 * 사용처:
 *   CustomAuthenticationSuccessHandler → 로그인 성공 시 토큰 저장
 *   JwtCookieLogoutHandler             → 로그아웃 시 토큰 삭제
 *   TokenReissueService                → AccessToken 재발급 시 저장
 */
@Component
@RequiredArgsConstructor
public class JwtCookieWriter {

    private final JwtCookieProperties cookieProperties;

    // AccessToken 쿠키 저장
    public void writeAccessToken(HttpServletResponse response, String token) {
        write(response, cookieProperties.accessTokenName(), token,
                cookieProperties.accessTokenMaxAge());
    }

    // RefreshToken 쿠키 저장
    public void writeRefreshToken(HttpServletResponse response, String token) {
        write(response, cookieProperties.refreshTokenName(), token,
                cookieProperties.refreshTokenMaxAge());
    }

    // AccessToken 쿠키 삭제 (MaxAge = 0 → 즉시 만료)
    public void clearAccessToken(HttpServletResponse response) {
        write(response, cookieProperties.accessTokenName(), "", 0);
    }

    // RefreshToken 쿠키 삭제 (MaxAge = 0 → 즉시 만료)
    public void clearRefreshToken(HttpServletResponse response) {
        write(response, cookieProperties.refreshTokenName(), "", 0);
    }

    // AccessToken + RefreshToken 동시 삭제 (로그아웃)
    public void clearAll(HttpServletResponse response) {
        clearAccessToken(response);
        clearRefreshToken(response);
    }

    // =========================================================================
    // 내부 공통 메서드
    // Cookie 클래스가 SameSite 미지원 → Set-Cookie 헤더 직접 작성
    // =========================================================================
    private void write(HttpServletResponse response, String name, String value, int maxAge) {

        // Set-Cookie 헤더 직접 구성
        // HttpOnly, Secure, SameSite 보안 3종 세트 적용
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value).append("; ");
        sb.append("Max-Age=").append(maxAge).append("; ");
        sb.append("Path=").append(cookieProperties.path()).append("; ");

        // domain 있을 때만 추가
        // 로컬 개발: domain 비워두면 자동으로 현재 도메인 적용
        if (cookieProperties.domain() != null
                && !cookieProperties.domain().isBlank()) {
            sb.append("Domain=").append(cookieProperties.domain()).append("; ");
        }

        // HttpOnly: JS에서 토큰 접근 차단 (XSS 방어)
        if (cookieProperties.httpOnly()) {
            sb.append("HttpOnly; ");
        }

        // Secure: HTTPS 에서만 전송
        // 운영: true / 로컬: false
        if (cookieProperties.secure()) {
            sb.append("Secure; ");
        }

        // SameSite: CSRF 방어
        // Strict → 외부 사이트 요청 시 쿠키 전송 안 함
        // Lax    → GET 요청은 허용
        // None   → 모든 요청 허용 (Secure 필수)
        sb.append("SameSite=").append(cookieProperties.sameSite());

        response.addHeader("Set-Cookie", sb.toString());
    }
}
