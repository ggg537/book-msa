package com.a.user.repository;

import com.a.user.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 회원 ID 로 조회 (로그인 성공 시 존재 여부 확인)
    Optional<RefreshToken> findByMemberId(Long memberId);

    // 토큰 값으로 조회 (재발급 검증)
    Optional<RefreshToken> findByToken(String token);

    // 회원 ID 로 삭제 (필요 시)
    void deleteByMemberId(Long memberId);
}
