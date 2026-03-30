package com.a.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token_tbl")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long memberId;

    @Column(nullable = false, length = 500)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean isValid;

    // ===== 정적 팩토리 메서드 =====
    public static RefreshToken of(Long memberId, String token, LocalDateTime expiresAt) {
        RefreshToken rt  = new RefreshToken();
        rt.memberId      = memberId;
        rt.token         = token;
        rt.expiresAt     = expiresAt;
        rt.isValid       = true;
        return rt;
    }

    // ===== 토큰 갱신 (재로그인 시) =====
    public void updateToken(String token, LocalDateTime expiresAt) {
        this.token     = token;
        this.expiresAt = expiresAt;
        this.isValid   = true;
    }

    // ===== 토큰 무효화 (로그아웃 시) =====
    public void invalidate() {
        this.isValid = false;
    }

    // ===== 만료 여부 확인 =====
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
