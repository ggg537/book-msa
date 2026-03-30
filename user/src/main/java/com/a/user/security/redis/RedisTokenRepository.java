package com.a.user.security.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 JWT 토큰 저장소
 *
 * 역할:
 *   ① RefreshToken 저장/조회/삭제  → 로그인, 재발급, 로그아웃
 *   ② AccessToken 블랙리스트 관리  → 로그아웃 후 만료 전 토큰 차단
 *
 * Key 네이밍 규칙 (다른 서버와 충돌 방지)
 *   user:refresh:{userId}      → RefreshToken
 *   user:blacklist:{token}     → 블랙리스트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    // Key prefix (다른 서버 Redis key 와 충돌 방지)
    private static final String REFRESH_PREFIX    = "user:refresh:";
    private static final String BLACKLIST_PREFIX  = "user:blacklist:";

    // =========================================================================
    // RefreshToken 저장
    // 로그인 성공 시 CustomAuthenticationSuccessHandler 에서 호출
    //
    // key:   user:refresh:{userId}
    // value: RefreshToken 문자열
    // TTL:   RefreshToken 만료시간 (7일)
    //        → 만료되면 Redis 에서 자동 삭제
    // =========================================================================
    public void saveRefreshToken(Long userId, String refreshToken, long expirationMs) {
        String key = REFRESH_PREFIX + userId;
        redisTemplate.opsForValue().set(
                key,
                refreshToken,
                expirationMs,
                TimeUnit.MILLISECONDS
        );
        log.debug("RefreshToken 저장 - userId: {}", userId);
    }

    // =========================================================================
    // RefreshToken 조회
    // TokenReissueService 에서 재발급 요청 시 호출
    // Redis 저장값과 요청값 비교해서 유효성 확인
    //
    // @return 저장된 RefreshToken (없으면 null)
    // =========================================================================
    public String getRefreshToken(Long userId) {
        return redisTemplate.opsForValue().get(REFRESH_PREFIX + userId);
    }

    // =========================================================================
    // RefreshToken 삭제
    // 로그아웃 시 CustomLogoutSuccessHandler 에서 호출
    // =========================================================================
    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(REFRESH_PREFIX + userId);
        log.debug("RefreshToken 삭제 - userId: {}", userId);
    }

    // =========================================================================
    // RefreshToken 존재 여부 확인
    // =========================================================================
    public boolean hasRefreshToken(Long userId) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(REFRESH_PREFIX + userId)
        );
    }

    // =========================================================================
    // AccessToken 블랙리스트 등록
    // 로그아웃 시 호출
    // AccessToken 은 만료 전까지 유효하므로
    // 로그아웃 후에도 토큰이 살아있으면 재사용 가능
    // → 블랙리스트에 등록해서 차단
    //
    // key:   user:blacklist:{accessToken}
    // value: "logout" (의미없는 값, key 존재 여부만 확인)
    // TTL:   AccessToken 남은 만료시간
    //        → 만료되면 어차피 못 쓰니까 자동 삭제
    // =========================================================================
    public void addBlacklist(String accessToken, long remainingMs) {
        if (remainingMs <= 0) return;  // 이미 만료된 토큰은 등록 불필요

        String key = BLACKLIST_PREFIX + accessToken;
        redisTemplate.opsForValue().set(
                key,
                "logout",
                remainingMs,
                TimeUnit.MILLISECONDS
        );
        log.debug("AccessToken 블랙리스트 등록");
    }

    // =========================================================================
    // AccessToken 블랙리스트 여부 확인
    // JwtAuthenticationFilter 에서 매 요청마다 호출
    // 블랙리스트에 있으면 → 로그아웃된 토큰 → 401 응답
    // =========================================================================
    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken)
        );
    }
}
