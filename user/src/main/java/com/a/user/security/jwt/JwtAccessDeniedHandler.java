package com.a.user.security.jwt;

import com.a.user.security.util.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 인증은 됐지만 권한이 없는 접근 처리
 *
 * 역할:
 *   로그인은 했지만 권한이 없는 API 접근 시
 *   403 응답 반환
 *
 * CustomAuthenticationEntryPoint 와 차이:
 *   EntryPoint         = 로그인 자체를 안 한 경우 → 401
 *   AccessDeniedHandler = 로그인 했지만 권한 없는 경우 → 403
 *
 * 흐름:
 *   일반 유저(ROLE_USER) 가
 *   → GET /api/admin/members (관리자 전용 API)
 *   → JwtAuthenticationFilter (토큰 유효 → 통과)
 *   → SecurityConfig 권한 체크
 *     → ROLE_ADMIN 필요한데 ROLE_USER 임
 *   → JwtAccessDeniedHandler (여기)
 *   → 403 응답
 *
 * SecurityConfig 등록:
 *   .exceptionHandling(ex -> ex
 *       .accessDeniedHandler(jwtAccessDeniedHandler)
 *   )
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * 권한 없는 요청 접근 시 자동 호출
     *
     * @param accessDeniedException 권한 없음 예외
     */
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        log.warn("권한 없는 접근 - uri: {}, ip: {}",
                request.getRequestURI(),
                WebUtil.getClientIp(request));

        WebUtil.writeJsonResponse(response, HttpStatus.FORBIDDEN.value(),
                Map.of("message", "접근 권한이 없습니다.")
        );
    }
}
