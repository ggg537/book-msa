package com.a.user.repository;

import com.a.user.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 이메일로 조회 (로그인, CustomUserDetailsService)
    Optional<Member> findByEmail(String email);

    // 이메일 중복 확인 (회원가입)
    boolean existsByEmail(String email);

    // 닉네임 중복 확인
    boolean existsByNickname(String nickname);
}
