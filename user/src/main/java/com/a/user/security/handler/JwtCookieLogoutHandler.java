package com.a.user.security.handler;

import com.a.user.security.jwt.JwtCookieWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

/**
 * 로그아웃 시 JWT 쿠키 삭제 핸들러
 *
 * 역할:
 *   로그아웃 요청 시 AccessToken, RefreshToken 쿠키 삭제
 *
 * LogoutHandler vs LogoutSuccessHandler 차이:
 *   LogoutHandler        = 로그아웃 과정 중 실행 (여러 개 체인)
 *   LogoutSuccessHandler = 모든 처리 끝난 후 최종 응답 (1번만)
 *
 * 실행 순서:
 *   POST /api/auth/logout
 *   → JwtCookieLogoutHandler (여기) ← 쿠키 삭제
 *   → CustomLogoutSuccessHandler    ← Redis 삭제 + 블랙리스트 + 응답
 *
 * SecurityConfig 등록:
 *   .logout(logout -> logout
 *       .addLogoutHandler(jwtCookieLogoutHandler)        ← 여기 등록
 *       .logoutSuccessHandler(customLogoutSuccessHandler)
 *   )
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtCookieLogoutHandler implements LogoutHandler {

    private final JwtCookieWriter jwtCookieWriter;

    /**
     * 로그아웃 요청 시 자동 호출
     * CustomLogoutSuccessHandler 보다 먼저 실행됨
     *
     * @param authentication 현재 로그인한 사용자 인증 정보
     *                       null 일 수 있음 (토큰 만료 상태)
     */
    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        // AccessToken + RefreshToken 쿠키 동시 삭제
        // JwtCookieWriter.clearAll() 위임
        jwtCookieWriter.clearAll(response);
        log.debug("JWT 쿠키 삭제 완료");
    }
}
