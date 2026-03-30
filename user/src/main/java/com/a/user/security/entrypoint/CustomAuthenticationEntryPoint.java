package com.a.user.security.entrypoint;

import com.a.user.security.util.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 인증되지 않은 사용자의 접근 처리
 *
 * 역할:
 *   로그인 안 한 사용자가 인증 필요한 API 접근 시
 *   401 응답 반환
 *
 * CustomAuthenticationFailureHandler 와 차이:
 *   FailureHandler  = 로그인 시도했는데 실패 (비밀번호 틀림 등)
 *   EntryPoint      = 로그인 자체를 안 하고 API 접근
 *
 * 흐름:
 *   로그인 안 한 사용자
 *   → GET /api/members/me (인증 필요 API)
 *   → JwtAuthenticationFilter (토큰 없음 → 통과)
 *   → SecurityConfig 권한 체크 (인증 정보 없음)
 *   → CustomAuthenticationEntryPoint (여기)
 *   → 401 응답
 *
 * SecurityConfig 등록:
 *   .exceptionHandling(ex -> ex
 *       .authenticationEntryPoint(customAuthenticationEntryPoint)
 *   )
 */
@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * 인증 안 된 요청 접근 시 자동 호출
     *
     * @param authException 인증 실패 예외
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        log.warn("인증되지 않은 접근 - uri: {}, ip: {}",
                request.getRequestURI(),
                WebUtil.getClientIp(request));

        WebUtil.writeJsonResponse(response, HttpStatus.UNAUTHORIZED.value(),
                Map.of("message", "로그인이 필요합니다.")
        );
    }
}
