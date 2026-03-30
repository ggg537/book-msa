package com.a.user.security.service;

import com.a.user.domain.RefreshToken;
import com.a.user.repository.RefreshTokenRepository;
import com.a.user.security.details.CustomUserDetails;
import com.a.user.security.details.CustomUserDetailsService;
import com.a.user.security.handler.RefreshExceptionHandler;
import com.a.user.security.jwt.JwtCookieResolver;
import com.a.user.security.jwt.JwtCookieWriter;
import com.a.user.security.jwt.JwtTokenProvider;
import com.a.user.security.redis.RedisTokenRepository;
import com.a.user.security.token.CustomAuthenticationToken;
import com.a.user.security.util.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;

/**
 * AccessToken 재발급 서비스
 *
 * 역할:
 *   AccessToken 만료 시
 *   쿠키의 RefreshToken 으로 새 AccessToken 재발급
 *
 * 검증 순서:
 *   ① 쿠키에서 RefreshToken 꺼냄
 *   ② 토큰 유효성 검증 (만료/위조)
 *   ③ Redis 저장값과 비교 (빠른 1차 검증)
 *   ④ DB 저장값과 비교  (정확한 2차 검증)
 *   ⑤ DB isValid 확인  (강제 로그아웃 여부)
 *   ⑥ 새 AccessToken 발급
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenReissueService {

    private final JwtTokenProvider         jwtTokenProvider;
    private final JwtCookieResolver        jwtCookieResolver;
    private final JwtCookieWriter          jwtCookieWriter;
    private final RedisTokenRepository     redisTokenRepository;
    private final RefreshTokenRepository   refreshTokenRepository;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshExceptionHandler  refreshExceptionHandler;

    /**
     * AccessToken 재발급
     * TokenReissueController 에서 호출
     */
    @Transactional
    public void reissue(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        // ① 쿠키에서 RefreshToken 꺼냄
        String refreshToken = jwtCookieResolver.resolveRefreshToken(request)
                .orElse(null);

        if (refreshToken == null) {
            refreshExceptionHandler.handleMissingRefreshToken(request, response);
            return;
        }

        // ② 토큰 유효성 검증 (만료/위조 확인)
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            refreshExceptionHandler.handleInvalidRefreshToken(request, response);
            return;
        }

        // ③ 토큰에서 userId, email 추출
        Long   userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String email  = jwtTokenProvider.getEmailFromToken(refreshToken);

        // ④ Redis 저장값과 비교 (1차 검증 - 빠름)
        String redisToken = redisTokenRepository.getRefreshToken(userId);
        if (redisToken == null || !redisToken.equals(refreshToken)) {
            refreshExceptionHandler.handleTokenMismatch(request, response);
            return;
        }

        // ⑤ DB 저장값 확인 (2차 검증 - 강제 로그아웃 여부)
        RefreshToken dbToken = refreshTokenRepository
                .findByMemberId(userId)
                .orElse(null);

        if (dbToken == null
                || !dbToken.getToken().equals(refreshToken)
                || !dbToken.isValid()
                || dbToken.isExpired()) {
            // DB 에서 무효화된 토큰 → Redis 도 삭제
            redisTokenRepository.deleteRefreshToken(userId);
            refreshExceptionHandler.handleTokenMismatch(request, response);
            return;
        }

        // ⑥ DB 에서 유저 조회 (최신 계정 상태 확인)
        CustomUserDetails userDetails =
                (CustomUserDetails) userDetailsService.loadUserByUsername(email);

        // ⑦ 새 AccessToken 발급
        CustomAuthenticationToken authentication =
                CustomAuthenticationToken.authenticated(
                        userDetails,
                        userId,
                        email,
                        userDetails.getAuthorities()
                );
        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);

        // ⑧ 새 AccessToken 쿠키 저장
        jwtCookieWriter.writeAccessToken(response, newAccessToken);

        log.info("AccessToken 재발급 성공 - userId: {}, ip: {}",
                userId, WebUtil.getClientIp(request));

        // ⑨ 성공 응답
        WebUtil.writeJsonResponse(response, HttpStatus.OK.value(),
                Map.of("message", "토큰이 재발급되었습니다.")
        );
    }
}
