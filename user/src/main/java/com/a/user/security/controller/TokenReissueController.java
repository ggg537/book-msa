package com.a.user.security.controller;

import com.a.user.security.service.TokenReissueService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * AccessToken 재발급 컨트롤러
 *
 * 역할:
 *   POST /api/auth/reissue 엔드포인트 제공
 *   TokenReissueService 에 위임
 *
 * 흐름:
 *   프론트엔드 → 401 감지
 *   → POST /api/auth/reissue 자동 요청
 *   → TokenReissueController (여기)
 *   → TokenReissueService
 *       → RefreshToken 검증
 *       → 새 AccessToken 발급
 *       → 쿠키 저장
 *   → 프론트엔드 원래 요청 재시도
 *
 * SecurityConfig:
 *   /api/auth/reissue → permitAll() 설정 필요
 *   (AccessToken 없이 요청하는 API 이므로)
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class TokenReissueController {

    private final TokenReissueService tokenReissueService;

    /**
     * AccessToken 재발급
     * AccessToken 만료 시 프론트엔드에서 자동 호출
     *
     * 인증 불필요 (permitAll)
     * → AccessToken 이 만료된 상태에서 요청하는 API
     * → RefreshToken 으로 검증
     */
    @PostMapping("/reissue")
    public void reissue(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        tokenReissueService.reissue(request, response);
    }
}
