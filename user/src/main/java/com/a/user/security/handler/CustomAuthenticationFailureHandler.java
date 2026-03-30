package com.a.user.security.handler;

import com.a.user.security.util.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 로그인 실패 시 처리 핸들러
 *
 * 역할:
 *   CustomAuthenticationProvider 인증 실패 후 호출
 *   실패 원인별로 다른 에러 메시지 응답
 *
 * 실패 원인 종류:
 *   BadCredentialsException → 이메일/비밀번호 불일치
 *   DisabledException       → 비활성화된 계정 (탈퇴/정지)
 *   LockedException         → 잠긴 계정
 *   그 외                   → 알 수 없는 오류
 *
 * 흐름:
 *   CustomAuthenticationProvider (인증 실패 → Exception 발생)
 *   → CustomAuthenticationFailureHandler (여기)
 *   → 실패 원인별 에러 응답 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler
        implements AuthenticationFailureHandler {

    /**
     * 로그인 실패 시 자동 호출
     *
     * @param exception CustomAuthenticationProvider 에서 발생한 예외
     *                  BadCredentialsException / DisabledException / LockedException 등
     */
    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        String clientIp = WebUtil.getClientIp(request);

        // 실패 원인별 메시지 + 상태코드 분기
        String message;
        int    status;

        if (exception instanceof BadCredentialsException) {
            // 이메일 없음 or 비밀번호 불일치
            message = "이메일 또는 비밀번호가 일치하지 않습니다.";
            status  = HttpStatus.UNAUTHORIZED.value();
            log.warn("로그인 실패 (자격증명 불일치) - ip: {}", clientIp);

        } else if (exception instanceof DisabledException) {
            // 탈퇴/정지 회원
            message = "비활성화된 계정입니다. 고객센터에 문의해주세요.";
            status  = HttpStatus.FORBIDDEN.value();
            log.warn("로그인 실패 (비활성화 계정) - ip: {}", clientIp);

        } else if (exception instanceof LockedException) {
            // 잠긴 계정
            message = "잠긴 계정입니다. 고객센터에 문의해주세요.";
            status  = HttpStatus.FORBIDDEN.value();
            log.warn("로그인 실패 (잠긴 계정) - ip: {}", clientIp);

        } else {
            // 그 외 알 수 없는 오류
            message = "로그인 처리 중 오류가 발생했습니다.";
            status  = HttpStatus.INTERNAL_SERVER_ERROR.value();
            log.error("로그인 실패 (알 수 없는 오류) - ip: {}, error: {}",
                    clientIp, exception.getMessage());
        }

        // 에러 응답 반환
        WebUtil.writeJsonResponse(response, status,
                Map.of("message", message)
        );
    }
}
