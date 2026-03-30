package com.a.user.security.support;

import org.springframework.security.core.Authentication;

/**
 * SecurityContext 에서 현재 로그인한 사용자 정보 추출 인터페이스
 *
 * 사용처:
 *   Controller / Service 에서
 *   "지금 누가 요청했는지" 알아야 할 때 사용
 *
 *   예시:
 *     내 정보 조회, 내 주문 목록, 내 찜 목록 등
 *     로그인한 사용자 본인의 데이터 조회 시
 */
public interface PrincipalExtractor {

    // 현재 로그인한 사용자 이메일 추출
    String extractEmail(Authentication authentication);

    // 현재 로그인한 사용자 ID 추출
    Long extractUserId(Authentication authentication);
}
