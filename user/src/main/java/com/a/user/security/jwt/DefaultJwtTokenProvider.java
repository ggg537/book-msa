package com.a.user.security.jwt;

import com.a.user.security.details.CustomUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JwtTokenProvider 구현체
 *
 * JWT 구조:
 *   Header:  {"alg": "HS256"}
 *   Payload: {"sub": email, "userId": 1, "role": "USER", "iat": ..., "exp": ...}
 *   Signature: HMAC-SHA256(secretKey)
 */
@Slf4j
@Component
public class DefaultJwtTokenProvider implements JwtTokenProvider {

    private final SecretKey     secretKey;
    private final JwtProperties jwtProperties;

    public DefaultJwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        // Base64 인코딩된 시크릿 키 → SecretKey 변환
        this.secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtProperties.getSecretKey())
        );
    }

    /**
     * AccessToken 생성
     * role 포함 (인가 처리에 사용)
     */
    @Override
    public String generateAccessToken(Authentication authentication) {
        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();
        return buildToken(
                userDetails.getEmail(),
                userDetails.getUserId(),
                userDetails.getRole(),
                jwtProperties.getAccessTokenExpiration()
        );
    }

    /**
     * RefreshToken 생성
     * role 미포함 (재발급에만 사용)
     */
    @Override
    public String generateRefreshToken(Authentication authentication) {
        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();
        return buildToken(
                userDetails.getEmail(),
                userDetails.getUserId(),
                null,   // role 없음
                jwtProperties.getRefreshTokenExpiration()
        );
    }

    /**
     * 토큰 공통 생성 메서드
     * role = null 이면 claim 에 추가 안 함 (RefreshToken)
     */
    private String buildToken(
            String email,
            Long userId,
            String role,
            long expMs
    ) {
        Date now = new Date();
        JwtBuilder builder = Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expMs))
                .signWith(secretKey);

        if (role != null) {
            builder.claim("role", role);
        }

        return builder.compact();
    }

    /**
     * 토큰 유효성 검증
     * 예외 종류별 로그 출력
     */
    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 토큰: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 토큰: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("잘못된 형식의 토큰: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("토큰 검증 실패: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    @Override
    public Long getUserIdFromToken(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    @Override
    public long getExpiration(String token) {
        Date expiration = getClaims(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
