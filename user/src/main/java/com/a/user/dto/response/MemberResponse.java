package com.a.user.dto.response;

import com.a.user.domain.Member;
import com.a.user.domain.Role;

import java.time.LocalDateTime;

/**
 * 회원 정보 응답 DTO
 * 비밀번호 등 민감 정보 제외
 *
 * 사용처:
 *   AuthController    → 회원가입 응답
 *   MemberController  → 내 정보 조회/수정 응답
 */
public record MemberResponse(
        Long          id,
        String        email,
        String        name,
        String        nickname,
        String        phone,
        Role          role,
        String        zipCode,
        String        address,
        String        addressDetail,
        boolean       isActive,
        int           point,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
    /**
     * Member 엔티티 → MemberResponse 변환
     * 정적 팩토리 메서드
     */
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                member.getPhone(),
                member.getRole(),
                member.getZipCode(),
                member.getAddress(),
                member.getAddressDetail(),
                member.isActive(),
                member.getPoint(),
                member.getLastLoginAt(),
                member.getCreatedAt()
        );
    }
}
