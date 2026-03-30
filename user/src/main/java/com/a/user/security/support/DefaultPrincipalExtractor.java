package com.a.user.security.support;

import com.a.user.security.details.CustomUserDetails;
import com.a.user.security.token.CustomAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * PrincipalExtractor 구현체
 * SecurityContext 의 Authentication 에서 유저 정보 추출
 *
 * 흐름:
 *   JwtAuthenticationFilter
 *   → SecurityContext 에 CustomAuthenticationToken 저장
 *   → Controller/Service 에서 PrincipalExtractor 로 꺼냄
 */
@Slf4j
@Component
public class DefaultPrincipalExtractor implements PrincipalExtractor {

    /**
     * 현재 로그인한 사용자 이메일 추출
     *
     * CustomAuthenticationToken 에 email 이 있으면 바로 꺼냄
     * 없으면 CustomUserDetails 에서 꺼냄 (fallback)
     */
    @Override
    public String extractEmail(Authentication authentication) {

        // CustomAuthenticationToken 인 경우 (JWT 인증 후)
        if (authentication instanceof CustomAuthenticationToken token) {
            return token.getEmail();
        }

        // fallback → CustomUserDetails 에서 꺼냄
        return getDetails(authentication).getEmail();
    }

    /**
     * 현재 로그인한 사용자 ID 추출
     *
     * CustomAuthenticationToken 에 userId 가 있으면 바로 꺼냄
     * 없으면 CustomUserDetails 에서 꺼냄 (fallback)
     */
    @Override
    public Long extractUserId(Authentication authentication) {

        // CustomAuthenticationToken 인 경우 (JWT 인증 후)
        if (authentication instanceof CustomAuthenticationToken token) {
            return token.getUserId();
        }

        // fallback → CustomUserDetails 에서 꺼냄
        return getDetails(authentication).getUserId();
    }

    /**
     * Authentication 에서 CustomUserDetails 꺼냄
     * principal 이 CustomUserDetails 가 아니면 ClassCastException 발생
     */
    private CustomUserDetails getDetails(Authentication authentication) {
        return (CustomUserDetails) authentication.getPrincipal();
    }
}
