package com.a.user.security.details;

import com.a.user.domain.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security 가 사용하는 유저 정보 객체
 *
 * 역할:
 *   Member 엔티티를 Spring Security 가 이해하는 형태로 변환
 *   인증/인가 처리 시 유저 정보를 담아서 전달하는 객체
 *
 * 사용처:
 *   CustomUserDetailsService     → loadUserByUsername() 에서 생성
 *   CustomAuthenticationProvider → 비밀번호 검증 시 사용
 *   DefaultJwtTokenProvider      → 토큰 생성 시 userId, email, role 추출
 *   CustomAuthenticationToken    → authenticated() 생성 시 principal 로 저장
 */
public class CustomUserDetails implements UserDetails {

    private final Member member;

    // 생성자 직접 호출 금지 → of() 정적 팩토리 메서드 사용
    private CustomUserDetails(Member member) {
        this.member = member;
    }

    /**
     * 정적 팩토리 메서드
     * CustomUserDetailsService.loadUserByUsername() 에서 호출
     */
    public static CustomUserDetails of(Member member) {
        return new CustomUserDetails(member);
    }

    // =========================================================================
    // 커스텀 getter
    // DefaultJwtTokenProvider, CustomAuthenticationProvider 에서 사용
    // =========================================================================

    /** 회원 고유 ID → JWT claim, SecurityContext 에 저장 */
    public Long getUserId() { return member.getId(); }

    /** 이메일 → JWT subject 로 사용 */
    public String getEmail() { return member.getEmail(); }

    /** 권한명 → JWT claim 으로 저장 ("USER", "ADMIN") */
    public String getRole() { return member.getRole().name(); }

    // =========================================================================
    // UserDetails 인터페이스 구현 (Spring Security 필수)
    // =========================================================================

    /** 이메일을 username 으로 사용 */
    @Override
    public String getUsername() { return member.getEmail(); }

    /** BCrypt 암호화된 비밀번호 */
    @Override
    public String getPassword() { return member.getPassword(); }

    /**
     * 권한 목록
     * "ROLE_" prefix 붙여서 반환 ("ROLE_USER", "ROLE_ADMIN")
     * Spring Security 권한 규칙: ROLE_ prefix 필수
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()));
    }

    /** 계정 만료 여부 (true = 만료 안 됨) */
    @Override
    public boolean isAccountNonExpired() { return true; }

    /** 계정 잠금 여부 (true = 잠금 안 됨) */
    @Override
    public boolean isAccountNonLocked() { return true; }

    /** 비밀번호 만료 여부 (true = 만료 안 됨) */
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    /**
     * 계정 활성화 여부
     * member.isActive() → 탈퇴/정지 회원이면 false
     * false 이면 CustomAuthenticationProvider 에서 DisabledException 발생
     */
    @Override
    public boolean isEnabled() { return member.isActive(); }
}
