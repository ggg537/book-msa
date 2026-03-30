package com.a.user.security.handler;

import com.a.user.security.jwt.JwtCookieWriter;
import com.a.user.security.util.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * RefreshToken 예외 처리 핸들러
 *
 * 역할:
 *   RefreshToken 만료 / 없음 / 불일치 시 처리
 *   쿠키 삭제 + 재로그인 유도 응답
 *
 * 사용처:
 *   TokenReissueService → RefreshToken 검증 실패 시 호출
 *
 * 흐름:
 *   AccessToken 만료
 *   → POST /api/auth/reissue 요청
 *   → TokenReissueService
 *       → RefreshToken 없음 / 만료 / Redis 불일치
 *       → RefreshExceptionHandler (여기)
 *           → 쿠키 전부 삭제
 *           → 재로그인 유도 응답
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshExceptionHandler {

    private final JwtCookieWriter jwtCookieWriter;

    /**
     * RefreshToken 없을 때
     * 쿠키에 RefreshToken 이 아예 없는 경우
     */
    public void handleMissingRefreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        log.warn("RefreshToken 없음 - ip: {}", WebUtil.getClientIp(request));
        sendResponse(request, response, "RefreshToken 이 없습니다. 다시 로그인해주세요.");
    }

    /**
     * RefreshToken 만료됐을 때
     * 유효하지 않은 토큰인 경우
     */
    public void handleInvalidRefreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        log.warn("RefreshToken 유효하지 않음 - ip: {}", WebUtil.getClientIp(request));
        sendResponse(request, response, "RefreshToken 이 유효하지 않습니다. 다시 로그인해주세요.");
    }

    /**
     * RefreshToken Redis 불일치
     * 쿠키의 토큰과 Redis 저장값이 다른 경우
     * → 토큰 탈취 가능성
     */
    public void handleTokenMismatch(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        log.warn("RefreshToken 불일치 (탈취 가능성) - ip: {}",
                WebUtil.getClientIp(request));
        sendResponse(request, response, "인증 정보가 일치하지 않습니다. 다시 로그인해주세요.");
    }

    // =========================================================================
    // 공통 처리
    // 쿠키 삭제 + 401 응답
    // =========================================================================
    private void sendResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            String message
    ) throws IOException {

        // 쿠키 전부 삭제 (AccessToken + RefreshToken)
        // 오염된 쿠키 정리
        jwtCookieWriter.clearAll(response);

        // 재로그인 유도 응답
        WebUtil.writeJsonResponse(response, HttpStatus.UNAUTHORIZED.value(),
                Map.of("message", message)
        );
    }
}
