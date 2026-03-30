package com.a.user.service;

import com.a.user.domain.Member;
import com.a.user.dto.request.MemberUpdateRequest;
import com.a.user.dto.request.PasswordChangeRequest;
import com.a.user.dto.response.MemberResponse;
import com.a.user.exception.BusinessException;
import com.a.user.exception.errorCode.ErrorCode;
import com.a.user.repository.MemberRepository;
import com.a.user.security.redis.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 정보 관리 서비스
 *
 * 역할:
 *   내 정보 조회, 회원 정보 수정, 비밀번호 변경, 탈퇴, 포인트 조회
 *
 * AuthService 와 차이:
 *   AuthService  → 회원가입 전용
 *   MemberService → 가입 후 회원 정보 관리 전용
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository     memberRepository;
    private final PasswordEncoder      passwordEncoder;
    private final RedisTokenRepository redisTokenRepository;

    /**
     * 내 정보 조회
     */
    @Transactional(readOnly = true)
    public MemberResponse getMyInfo(Long userId) {
        return MemberResponse.from(findMemberById(userId));
    }

    /**
     * 회원 정보 수정
     * 닉네임 변경 시 중복 확인
     */
    @Transactional
    public MemberResponse updateInfo(Long userId, MemberUpdateRequest request) {
        Member member = findMemberById(userId);

        if (!member.getNickname().equals(request.nickname())
                && memberRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.MEMBER_DUPLICATE_NICKNAME);
        }

        member.updateInfo(
                request.nickname(),
                request.phone(),
                request.zipCode(),
                request.address(),
                request.addressDetail()
        );

        log.info("회원 정보 수정 - userId: {}", userId);
        return MemberResponse.from(member);
    }

    /**
     * 비밀번호 변경
     * 현재 비밀번호 확인 후 새 비밀번호 BCrypt 암호화 저장
     */
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        Member member = findMemberById(userId);

        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.MEMBER_INVALID_PASSWORD);
        }

        member.updatePassword(passwordEncoder.encode(request.newPassword()));
        log.info("비밀번호 변경 - userId: {}", userId);
    }

    /**
     * 회원 탈퇴
     * isActive = false (실제 삭제 아님)
     * Redis RefreshToken 삭제 → 즉시 로그아웃
     */
    @Transactional
    public void deactivate(Long userId) {
        Member member = findMemberById(userId);
        member.deactivate();
        redisTokenRepository.deleteRefreshToken(userId);
        log.info("회원 탈퇴 - userId: {}", userId);
    }

    /**
     * 포인트 조회
     */
    @Transactional(readOnly = true)
    public int getPoint(Long userId) {
        return findMemberById(userId).getPoint();
    }

    // =========================================================================
    // 내부 공통 메서드
    // =========================================================================
    private Member findMemberById(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
