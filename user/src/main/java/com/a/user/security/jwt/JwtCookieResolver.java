package com.a.user.security.jwt;

import com.a.user.security.util.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 요청 쿠키에서 JWT 토큰을 꺼내는 전담 클래스
 *
 * 역할 분리:
 *   꺼내기 → JwtCookieResolver  (여기)
 *   검증   → JwtTokenProvider
 *   저장   → JwtCookieWriter
 *   삭제   → JwtCookieLogoutHandler
 */
@Component
@RequiredArgsConstructor
public class JwtCookieResolver {

    /**
     * 요청 쿠키에서 AccessToken 추출
     *
     * 사용처:
     *   JwtAuthenticationFilter → 매 API 요청마다 토큰 꺼내서 검증
     *
     * @return AccessToken 문자열 (쿠키 없으면 Optional.empty())
     */
    public Optional<String> resolveAccessToken(HttpServletRequest request) {
        return WebUtil.extractCookieValue(request, JwtCookieProperties.ACCESS_TOKEN);
    }

    /**
     * 요청 쿠키에서 RefreshToken 추출
     *
     * 사용처:
     *   TokenReissueService → AccessToken 만료 시 재발급 요청에서 사용
     *   RefreshToken 꺼낸 후 Redis에 저장된 값과 비교해서 유효성 확인
     *
     * @return RefreshToken 문자열 (쿠키 없으면 Optional.empty())
     */
    public Optional<String> resolveRefreshToken(HttpServletRequest request) {
        return WebUtil.extractCookieValue(request, JwtCookieProperties.REFRESH_TOKEN);
    }
}
