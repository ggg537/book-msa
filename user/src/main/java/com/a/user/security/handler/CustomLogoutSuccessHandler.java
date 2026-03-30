package com.a.user.security.handler;

import com.a.user.repository.RefreshTokenRepository;
import com.a.user.security.jwt.JwtCookieResolver;
import com.a.user.security.jwt.JwtTokenProvider;
import com.a.user.security.redis.RedisTokenRepository;
import com.a.user.security.token.CustomAuthenticationToken;
import com.a.user.security.util.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;

/**
 * 로그아웃 성공 시 처리 핸들러
 *
 * 역할:
 *   AccessToken 블랙리스트 등록  (Redis)
 *   RefreshToken 삭제            (Redis + DB)
 *   로그아웃 성공 응답
 *
 * 쿠키 삭제는 JwtCookieLogoutHandler 가 먼저 처리
 *
 * 흐름:
 *   POST /api/auth/logout
 *   → JwtCookieLogoutHandler     (쿠키 삭제)
 *   → CustomLogoutSuccessHandler (여기)
 *       → AccessToken 블랙리스트 등록
 *       → Redis RefreshToken 삭제
 *       → DB RefreshToken 무효화
 *       → 성공 응답
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final JwtTokenProvider        jwtTokenProvider;
    private final JwtCookieResolver       jwtCookieResolver;
    private final RedisTokenRepository    redisTokenRepository;
    private final RefreshTokenRepository  refreshTokenRepository;

    @Override
    @Transactional
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        // ① 쿠키에서 AccessToken 꺼냄 (블랙리스트 등록용)
        String accessToken = jwtCookieResolver.resolveAccessToken(request)
                .orElse(null);

        // ② AccessToken 블랙리스트 등록 (Redis)
        // 로그아웃 후 탈취된 토큰 재사용 차단
        // TTL = 남은 만료시간 → 만료되면 자동 삭제
        if (accessToken != null
                && jwtTokenProvider.validateToken(accessToken)) {
            long remainingMs = jwtTokenProvider.getExpiration(accessToken);
            redisTokenRepository.addBlacklist(accessToken, remainingMs);
        }

        // ③ userId 추출 후 RefreshToken 삭제
        if (authentication instanceof CustomAuthenticationToken token) {
            Long userId = token.getUserId();

            // Redis RefreshToken 삭제
            redisTokenRepository.deleteRefreshToken(userId);

            // DB RefreshToken 무효화 (isValid = false)
            // 실제 삭제 대신 무효화 → 로그아웃 이력 보관
            refreshTokenRepository.findByMemberId(userId)
                    .ifPresent(rt -> rt.invalidate());

            log.info("로그아웃 성공 - userId: {}, ip: {}",
                    userId, WebUtil.getClientIp(request));

        } else {
            log.info("로그아웃 성공 (인증 정보 없음) - ip: {}",
                    WebUtil.getClientIp(request));
        }

        // ④ 성공 응답
        WebUtil.writeJsonResponse(response, HttpStatus.OK.value(),
                Map.of("message", "로그아웃 되었습니다.")
        );
    }
}
