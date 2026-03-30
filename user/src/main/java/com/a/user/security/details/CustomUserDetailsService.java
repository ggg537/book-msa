package com.a.user.security.details;

import com.a.user.exception.BusinessException;
import com.a.user.exception.errorCode.ErrorCode;
import com.a.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security 가 유저 정보를 조회할 때 사용하는 서비스
 *
 * 역할:
 *   이메일로 DB 에서 회원 조회 후 CustomUserDetails 로 변환해서 반환
 *
 * 사용처:
 *   CustomAuthenticationProvider → authenticate() 에서 호출
 *   Spring Security 내부          → 인증 처리 시 자동 호출
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    /**
     * 이메일로 회원 조회
     *
     * @param email 로그인 요청 이메일
     * @return CustomUserDetails (Spring Security 유저 정보 객체)
     * @throws BusinessException 회원 없으면 MEMBER_NOT_FOUND 에러
     *
     * 흐름:
     *   CustomAuthenticationProvider.authenticate()
     *   → loadUserByUsername(email) 호출
     *   → DB 조회 → CustomUserDetails 반환
     *   → 비밀번호 검증 → 인증 완료
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return memberRepository.findByEmail(email)
                .map(CustomUserDetails::of)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
