package com.a.user.security.filter;

import com.a.user.security.token.CustomAuthenticationToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.util.Map;

/**
 * 로그인 요청 처리 필터
 *
 * 역할:
 *   POST /api/auth/login 요청을 가로채서
 *   email, password 꺼냄
 *   → CustomAuthenticationToken.unauthenticated() 생성
 *   → AuthenticationManager → CustomAuthenticationProvider 에게 전달
 *
 * 흐름:
 *   POST /api/auth/login { email, password }
 *   → attemptAuthentication()           ← 여기서 처리
 *   → CustomAuthenticationProvider      (검증)
 *   → 성공: CustomAuthenticationSuccessHandler
 *   → 실패: CustomAuthenticationFailureHandler
 */
@Slf4j
public class CustomAuthenticationFilter
        extends AbstractAuthenticationProcessingFilter {

    // 로그인 요청 URL + HTTP 메서드 지정
    // PathPatternRequestMatcher: Spring Security 6.x 권장 방식
    private static final RequestMatcher LOGIN_MATCHER =
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.POST, "/api/auth/login");

    private final ObjectMapper objectMapper;

    /**
     * 생성자
     * SecurityConfig 에서 직접 생성해서 등록
     *
     * @param authenticationManager CustomAuthenticationProvider 를 포함한 매니저
     * @param objectMapper          JSON 파싱용
     */
    public CustomAuthenticationFilter(
            AuthenticationManager authenticationManager,
            ObjectMapper objectMapper
    ) {
        super(LOGIN_MATCHER, authenticationManager);
        this.objectMapper = objectMapper;
    }

    /**
     * 로그인 인증 시도
     * POST /api/auth/login 요청 시 자동 호출
     *
     * @return CustomAuthenticationToken (unauthenticated)
     *         → AuthenticationManager → CustomAuthenticationProvider 로 전달
     * @throws AuthenticationException 요청 형식 오류 시
     */
    @Override
    @SuppressWarnings("unchecked")
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthenticationException, IOException {

        // ① POST 요청인지 확인
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            throw new AuthenticationServiceException(
                    "지원하지 않는 HTTP 메서드: " + request.getMethod());
        }

        // ② Content-Type 확인 (JSON 요청인지)
        String contentType = request.getContentType();
        if (contentType == null
                || !contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
            throw new AuthenticationServiceException(
                    "Content-Type 은 application/json 이어야 합니다.");
        }

        // ③ 요청 바디에서 email, password 꺼냄
        Map<String, String> body = objectMapper.readValue(
                request.getInputStream(), Map.class);

        String email    = body.getOrDefault("email", "").trim();
        String password = body.getOrDefault("password", "");

        // ④ email, password 빈값 검증
        if (email.isBlank()) {
            throw new AuthenticationServiceException("이메일을 입력해주세요.");
        }
        if (password.isBlank()) {
            throw new AuthenticationServiceException("비밀번호를 입력해주세요.");
        }

        log.debug("로그인 시도 - email: {}", email);

        // ⑤ unauthenticated 토큰 생성
        // → AuthenticationManager
        // → CustomAuthenticationProvider.authenticate() 호출
        return getAuthenticationManager().authenticate(
                CustomAuthenticationToken.unauthenticated(email, password)
        );
    }
}
