package com.a.user.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_tbl")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    // 배송 정보
    @Column(length = 10)
    private String zipCode;

    @Column(length = 200)
    private String address;

    @Column(length = 200)
    private String addressDetail;

    // 계정 상태
    @Column(nullable = false)
    private boolean isActive;

    private LocalDateTime lastLoginAt;

    // 포인트
    @Column(nullable = false)
    private int point;

    // ===== 비밀번호 변경 =====
    public void updatePassword(String password) {
        this.password = password;
    }

    // ===== 회원 정보 수정 =====
    public void updateInfo(String nickname, String phone,
                           String zipCode, String address, String addressDetail) {
        this.nickname     = nickname;
        this.phone        = phone;
        this.zipCode      = zipCode;
        this.address      = address;
        this.addressDetail = addressDetail;
    }

    // ===== 마지막 로그인 시간 갱신 =====
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    // ===== 탈퇴 처리 =====
    public void deactivate() {
        this.isActive = false;
    }

    // ===== 포인트 적립 =====
    public void addPoint(int point) {
        this.point += point;
    }

    // ===== 포인트 사용 =====
    public void usePoint(int point) {
        if (this.point < point) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
        this.point -= point;
    }

    // ===== 정적 팩토리 메서드 =====
    public static Member of(String email, String password, String name,
                            String nickname, String phone, Role role) {
        Member member    = new Member();
        member.email     = email;
        member.password  = password;
        member.name      = name;
        member.nickname  = nickname;
        member.phone     = phone;
        member.role      = role;
        member.isActive  = true;
        member.point     = 0;
        return member;
    }
}
