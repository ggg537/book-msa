package com.a.gateway.filter;

import com.a.gateway.jwt.JwtTokenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFilter implements GlobalFilter, Ordered {

    private final JwtTokenValidator jwtTokenValidator;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Value("${jwt.cookie.access-token-name}")
    private String accessTokenCookieName;

    // 인증 없이 접근 가능한 경로
    private static final List<String> PERMIT_ALL = List.of(
            "/api/auth/login",
            "/api/auth/signup",
            "/api/auth/reissue",
            "/api/auth/check-email",
            "/api/auth/check-nickname",
            "/",
            "/main",
            "/login",
            "/signup",
            "/error",
            "/favicon.ico"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        // STEP 1. 인증 불필요 경로 확인
        if (isPermitAll(path)) {
            log.debug("[Gateway] 인증 불필요 경로 통과: {}", path);
            return chain.filter(exchange);
        }

        // STEP 2. 쿠키에서 AccessToken 추출
        String token = extractToken(request);
        if (token == null) {
            log.debug("[Gateway] 토큰 없음 → 401: {}", path);
            return unauthorized(exchange.getResponse());
        }

        // STEP 3. JWT 유효성 검증
        if (!jwtTokenValidator.isValid(token)) {
            log.debug("[Gateway] 유효하지 않은 토큰 → 401: {}", path);
            return unauthorized(exchange.getResponse());
        }

        // STEP 4. Redis 블랙리스트 확인
        String blacklistKey = "user:blacklist:" + token;
        return reactiveRedisTemplate.hasKey(blacklistKey)
                .flatMap(isBlacklisted -> {
                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        log.debug("[Gateway] 블랙리스트 토큰 → 401: {}", path);
                        return unauthorized(exchange.getResponse());
                    }

                    // STEP 5. 검증 통과 → 헤더에 사용자 정보 추가
                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header("X-User-Email", jwtTokenValidator.getEmail(token))
                            .header("X-User-Id",    String.valueOf(jwtTokenValidator.getUserId(token)))
                            .header("X-User-Role",  jwtTokenValidator.getRole(token))
                            .build();

                    log.debug("[Gateway] 인증 통과 - email: {}, path: {}",
                            jwtTokenValidator.getEmail(token), path);

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                });
    }

    // 인증 불필요 경로 체크
    private boolean isPermitAll(String path) {
        return PERMIT_ALL.stream().anyMatch(path::startsWith)
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/");
    }

    // 쿠키에서 AccessToken 추출
    private String extractToken(ServerHttpRequest request) {
        HttpCookie cookie = request.getCookies().getFirst(accessTokenCookieName);
        return cookie != null ? cookie.getValue() : null;
    }

    // 401 응답 반환
    private Mono<Void> unauthorized(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    // 필터 실행 순서 (-1 = 최우선)
    @Override
    public int getOrder() {
        return -1;
    }
}