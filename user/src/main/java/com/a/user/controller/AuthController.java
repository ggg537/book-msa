package com.a.user.controller;

import com.a.user.dto.request.SignupRequest;
import com.a.user.dto.response.MemberResponse;
import com.a.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 인증 관련 컨트롤러
 *
 * 로그인   → CustomAuthenticationFilter 가 처리 (Controller 불필요)
 * 로그아웃  → SecurityConfig logout 설정 처리 (Controller 불필요)
 * 재발급   → TokenReissueController
 *
 * 전체 경로 permitAll (PermitAllPath 등록)
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<MemberResponse> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        MemberResponse response = authService.signup(request);
        log.info("회원가입 완료 - email: {}", request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 이메일 중복 확인
     * GET /api/auth/check-email?email=xxx
     *
     * @return available: true = 사용 가능 / false = 중복
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(
            @RequestParam String email
    ) {
        return ResponseEntity.ok(Map.of("available", authService.checkEmail(email)));
    }

    /**
     * 닉네임 중복 확인
     * GET /api/auth/check-nickname?nickname=xxx
     *
     * @return available: true = 사용 가능 / false = 중복
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNickname(
            @RequestParam String nickname
    ) {
        return ResponseEntity.ok(Map.of("available", authService.checkNickname(nickname)));
    }
}
