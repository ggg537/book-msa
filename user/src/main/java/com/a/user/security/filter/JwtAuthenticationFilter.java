package com.a.user.security.filter;

import com.a.user.security.details.CustomUserDetails;
import com.a.user.security.details.CustomUserDetailsService;
import com.a.user.security.jwt.JwtCookieResolver;
import com.a.user.security.jwt.JwtTokenProvider;
import com.a.user.security.redis.RedisTokenRepository;
import com.a.user.security.token.CustomAuthenticationToken;
import com.a.user.security.util.WebUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * JWT 토큰 검증 필터
 * 로그인 이후 모든 API 요청마다 실행
 *
 * 역할:
 *   쿠키에서 AccessToken 꺼냄
 *   → 블랙리스트 확인 (로그아웃된 토큰 차단)
 *   → 토큰 유효성 검증
 *   → SecurityContext 에 인증 정보 저장
 *   → API 통과
 *
 * OncePerRequestFilter:
 *   요청당 딱 1번만 실행 보장
 *   (forward, include 등으로 중복 실행 방지)
 *
 * 실행 순서:
 *   모든 요청
 *   → JwtAuthenticationFilter (여기)
 *   → DispatcherServlet
 *   → Controller
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtCookieResolver jwtCookieResolver;
    private final RedisTokenRepository redisTokenRepository;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // ① 쿠키에서 AccessToken 꺼냄
        // 없으면 인증 없이 다음 필터로 통과
        // (인증 필요한 API 면 SecurityConfig 에서 차단)
        String accessToken = jwtCookieResolver.resolveAccessToken(request)
                .orElse(null);

        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // ② 블랙리스트 확인
        // 로그아웃된 토큰으로 재사용 시도 차단
        if (redisTokenRepository.isBlacklisted(accessToken)) {
            log.warn("블랙리스트 토큰 사용 시도 - ip: {}",
                    WebUtil.getClientIp(request));
            WebUtil.writeJsonResponse(response, HttpStatus.UNAUTHORIZED.value(),
                    Map.of("message", "로그인이 필요합니다."));
            return;
        }

        // ③ 토큰 유효성 검증
        // 만료 / 위조 / 형식오류 확인
        if (!jwtTokenProvider.validateToken(accessToken)) {
            log.warn("유효하지 않은 토큰 - ip: {}",
                    WebUtil.getClientIp(request));
            WebUtil.writeJsonResponse(response, HttpStatus.UNAUTHORIZED.value(),
                    Map.of("message", "토큰이 유효하지 않습니다."));
            return;
        }

        // ④ 토큰에서 유저 정보 추출
        String email  = jwtTokenProvider.getEmailFromToken(accessToken);
        Long   userId = jwtTokenProvider.getUserIdFromToken(accessToken);

        // ⑤ DB 에서 유저 조회
        // 탈퇴/정지 등 계정 상태 최신화
        CustomUserDetails userDetails =
                (CustomUserDetails) userDetailsService.loadUserByUsername(email);

        // ⑥ SecurityContext 에 인증 정보 저장
        // 이후 Controller / Service 에서 꺼내 사용
        CustomAuthenticationToken authentication =
                CustomAuthenticationToken.authenticated(
                        userDetails,
                        userId,
                        email,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("인증 성공 - email: {}, userId: {}", email, userId);

        // ⑦ 다음 필터로 통과
        filterChain.doFilter(request, response);
    }
}
