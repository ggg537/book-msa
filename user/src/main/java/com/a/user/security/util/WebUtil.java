package com.a.user.security.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

/**
 * HTTP 요청/응답 관련 공통 유틸리티
 * 필터, 핸들러, EntryPoint 등 Security 레이어 전반에서 사용
 */
public final class WebUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Authorization 헤더 Bearer 접두사
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    // AJAX 요청 판단용 헤더
    private static final String X_REQUESTED_WITH = "X-Requested-With";
    private static final String XML_HTTP_REQUEST = "XMLHttpRequest";

    // 실제 클라이언트 IP 추출용 헤더 (우선순위 순서)
    // 프록시/로드밸런서 환경에서 실제 IP가 이 헤더들에 담김
    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",       // 표준 프록시 헤더 (가장 많이 사용)
            "Proxy-Client-IP",       // Apache 프록시
            "WL-Proxy-Client-IP",    // WebLogic 프록시
            "HTTP_X_FORWARDED_FOR",  // 일부 프록시
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "X-Real-IP",             // Nginx 리버스 프록시
            "REMOTE_ADDR"
    };

    // 유효하지 않은 IP 판단 기준
    private static final String UNKNOWN_IP = "unknown";
    private static final int MAX_IP_LENGTH = 45; // IPv6 최대 길이

    // 인스턴스 생성 방지 (유틸 클래스)
    private WebUtil() {
        throw new UnsupportedOperationException("WebUtil은 인스턴스 생성 불가");
    }

    // =========================================================================
    // JSON 응답 작성
    // =========================================================================

    /**
     * JSON 응답 작성
     * 핸들러, 필터, EntryPoint 에서 공통으로 사용
     *
     * @param response HTTP 응답 객체
     * @param status   HTTP 상태 코드 (200, 401, 403 등)
     * @param body     응답 바디 객체 (자동으로 JSON 직렬화)
     */
    public static void writeJsonResponse(
            HttpServletResponse response,
            int status,
            Object body
    ) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // XSS 방지 헤더 추가
        response.setHeader("X-Content-Type-Options", "nosniff");

        response.getWriter().write(objectMapper.writeValueAsString(body));
        response.getWriter().flush();
    }

    // =========================================================================
    // 요청 타입 판단
    // =========================================================================

    /**
     * AJAX 요청 여부 판단
     * 로그인 실패/성공 핸들러에서
     * AJAX면 JSON 응답, 일반 요청이면 redirect 처리 분기에 사용
     */
    public static boolean isAjax(HttpServletRequest request) {
        String xRequestedWith = request.getHeader(X_REQUESTED_WITH);
        String accept = request.getHeader("Accept");
        String contentType = request.getHeader("Content-Type");

        boolean isXRequestedWith = XML_HTTP_REQUEST.equalsIgnoreCase(xRequestedWith);
        boolean isAcceptJson = accept != null
                && accept.contains(MediaType.APPLICATION_JSON_VALUE);
        boolean isContentTypeJson = contentType != null
                && contentType.contains(MediaType.APPLICATION_JSON_VALUE);

        return isXRequestedWith || isAcceptJson || isContentTypeJson;
    }

    // =========================================================================
    // JWT 토큰 추출
    // =========================================================================

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     * JwtAuthenticationFilter 에서 API 요청마다 호출
     *
     * Authorization: Bearer eyJhbGci...
     * → eyJhbGci... 만 반환
     *
     * @return 토큰 문자열 (없으면 Optional.empty())
     */
    public static Optional<String> extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);

        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();

        // 빈 토큰 방어
        if (token.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(token);
    }

    /**
     * 쿠키에서 특정 이름의 값 추출
     * JwtCookieResolver 에서 사용
     *
     * @param request    HTTP 요청
     * @param cookieName 찾을 쿠키 이름
     * @return 쿠키 값 (없으면 Optional.empty())
     */
    public static Optional<String> extractCookieValue(
            HttpServletRequest request,
            String cookieName
    ) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> !value.isBlank())
                .findFirst();
    }

    // =========================================================================
    // 클라이언트 IP 추출
    // =========================================================================

    /**
     * 클라이언트 실제 IP 추출
     * 로그인 실패 로그, 어뷰징 감지, 보안 감사 로그에 사용
     *
     * 운영 환경에서는 로드밸런서/프록시를 거치기 때문에
     * request.getRemoteAddr() 는 프록시 IP를 반환함
     * X-Forwarded-For 등의 헤더에서 실제 클라이언트 IP 추출 필요
     *
     * 보안 주의:
     * X-Forwarded-For 헤더는 클라이언트가 위조 가능
     * 신뢰하는 프록시 IP 목록을 관리하거나
     * AWS ALB 등 인프라 레벨에서 헤더 검증 필요
     */
    public static String getClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);

            if (isValidIp(ip)) {
                // X-Forwarded-For는 여러 IP가 콤마로 이어짐
                // "client, proxy1, proxy2" → 첫 번째가 실제 클라이언트
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }

                if (isValidIp(ip)) {
                    return ip;
                }
            }
        }

        // 모든 헤더에서 못 찾으면 직접 연결 IP 반환
        return request.getRemoteAddr();
    }

    /**
     * 유효한 IP 주소인지 검증
     * null, blank, "unknown", 너무 긴 값 방어
     */
    private static boolean isValidIp(String ip) {
        return ip != null
                && !ip.isBlank()
                && !UNKNOWN_IP.equalsIgnoreCase(ip)
                && ip.length() <= MAX_IP_LENGTH;
    }

    // =========================================================================
    // 쿠키 생성/삭제
    // =========================================================================

    /**
     * 보안 쿠키 생성
     * JwtCookieWriter 에서 사용
     *
     * 보안 설정:
     * - HttpOnly: JS에서 접근 불가 (XSS 방어)
     * - Secure: HTTPS에서만 전송 (운영 환경)
     * - SameSite=Strict: CSRF 방어
     * - Path=/: 전체 경로에서 유효
     */
    public static Cookie createSecureCookie(
            String name,
            String value,
            int maxAge,
            boolean secure     // 운영: true (HTTPS), 로컬: false
    ) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);   // XSS 방어 - JS 접근 차단
        cookie.setSecure(secure);   // HTTPS 전용
        cookie.setPath("/");        // 전체 경로 유효
        cookie.setMaxAge(maxAge);   // 만료 시간 (초)

        // SameSite 설정 (Spring Boot 3.x 방식)
        // Strict: 같은 사이트 요청만 쿠키 전송 (CSRF 방어)
        // Lax: 외부에서 GET 요청 허용
        // None: 모든 요청 허용 (Secure 필수)
        // → 쿠키 헤더에 직접 추가 (Cookie 클래스가 SameSite 미지원)
        return cookie;
    }

    /**
     * 쿠키 삭제 (MaxAge = 0으로 만료 처리)
     * 로그아웃 시 JwtCookieLogoutHandler 에서 사용
     */
    public static Cookie deleteCookie(String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);    // 즉시 만료
        return cookie;
    }

    /**
     * 응답 헤더에 SameSite 속성 추가
     * createSecureCookie() 와 함께 사용
     *
     * Set-Cookie: accessToken=xxx; HttpOnly; Secure; SameSite=Strict
     */
    public static void addSameSiteCookie(
            HttpServletResponse response,
            Cookie cookie,
            String sameSite    // "Strict", "Lax", "None"
    ) {
        response.addCookie(cookie);

        // Set-Cookie 헤더에 SameSite 직접 추가
        String cookieHeader = String.format(
                "%s=%s; Max-Age=%d; Path=%s; HttpOnly%s; SameSite=%s",
                cookie.getName(),
                cookie.getValue(),
                cookie.getMaxAge(),
                cookie.getPath(),
                cookie.getSecure() ? "; Secure" : "",
                sameSite
        );
        response.addHeader("Set-Cookie", cookieHeader);
    }
}
