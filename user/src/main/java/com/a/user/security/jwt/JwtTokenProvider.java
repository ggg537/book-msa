package com.a.user.security.jwt;

import org.springframework.security.core.Authentication;

/**
 * JWT 토큰 생성/검증 인터페이스
 *
 * 구현체: DefaultJwtTokenProvider
 *
 * 사용처:
 *   CustomAuthenticationSuccessHandler → 로그인 성공 시 토큰 생성
 *   JwtAuthenticationFilter            → 매 요청 토큰 검증
 *   TokenReissueService                → 재발급 시 토큰 검증/생성
 *   CustomLogoutSuccessHandler         → 로그아웃 시 남은 만료시간 조회
 */
public interface JwtTokenProvider {

    /**
     * AccessToken 생성 (만료: 30분)
     * subject: email, claim: userId, role
     */
    String generateAccessToken(Authentication authentication);

    /**
     * RefreshToken 생성 (만료: 7일)
     * subject: email, claim: userId (role 없음)
     */
    String generateRefreshToken(Authentication authentication);

    /**
     * 토큰 유효성 검증
     * 만료/위조/형식오류 확인
     *
     * @return true = 유효 / false = 무효
     */
    boolean validateToken(String token);

    /** 토큰 Payload 에서 이메일 추출 (subject) */
    String getEmailFromToken(String token);

    /** 토큰 Payload 에서 userId 추출 (claim) */
    Long getUserIdFromToken(String token);

    /**
     * 토큰 남은 만료시간 (ms)
     * 로그아웃 시 블랙리스트 TTL 설정에 사용
     */
    long getExpiration(String token);
}
