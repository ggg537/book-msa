package com.a.gateway.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;

@Slf4j
@Component
public class JwtTokenValidator {

    private final SecretKey secretKey;

    public JwtTokenValidator(@Value("${jwt.secret-key}") String secretKeyValue) {
        byte[] keyBytes = Base64.getDecoder().decode(secretKeyValue);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // 토큰 유효성 검증 (서명 + 만료시간)
    public boolean isValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("유효하지 않은 토큰: {}", e.getMessage());
            return false;
        }
    }

    // 토큰 페이로드 추출
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getEmail(String token)  { return getClaims(token).getSubject(); }
    public Long   getUserId(String token) { return getClaims(token).get("userId", Long.class); }
    public String getRole(String token)   { return getClaims(token).get("role", String.class); }
}