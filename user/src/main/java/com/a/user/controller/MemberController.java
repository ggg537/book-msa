package com.a.user.controller;

import com.a.user.dto.request.MemberUpdateRequest;
import com.a.user.dto.request.PasswordChangeRequest;
import com.a.user.dto.response.MemberResponse;
import com.a.user.security.support.DefaultPrincipalExtractor;
import com.a.user.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 회원 정보 관리 컨트롤러
 * 모든 API 인증 필요 (anyRequest().authenticated())
 *
 * PrincipalExtractor 로 SecurityContext 에서 userId 꺼내서 Service 에 전달
 */
@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService             memberService;
    private final DefaultPrincipalExtractor principalExtractor;

    /**
     * 내 정보 조회
     * GET /api/members/me
     */
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMyInfo(Authentication authentication) {
        Long userId = principalExtractor.extractUserId(authentication);
        return ResponseEntity.ok(memberService.getMyInfo(userId));
    }

    /**
     * 회원 정보 수정
     * PUT /api/members/me
     */
    @PutMapping("/me")
    public ResponseEntity<MemberResponse> updateInfo(
            Authentication authentication,
            @Valid @RequestBody MemberUpdateRequest request
    ) {
        Long userId = principalExtractor.extractUserId(authentication);
        return ResponseEntity.ok(memberService.updateInfo(userId, request));
    }

    /**
     * 비밀번호 변경
     * PUT /api/members/password
     */
    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            Authentication authentication,
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        Long userId = principalExtractor.extractUserId(authentication);
        memberService.changePassword(userId, request);
        return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다."));
    }

    /**
     * 회원 탈퇴
     * DELETE /api/members/me
     */
    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deactivate(
            Authentication authentication
    ) {
        Long userId = principalExtractor.extractUserId(authentication);
        memberService.deactivate(userId);
        return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 완료되었습니다."));
    }

    /**
     * 포인트 조회
     * GET /api/members/point
     */
    @GetMapping("/point")
    public ResponseEntity<Map<String, Integer>> getPoint(
            Authentication authentication
    ) {
        Long userId = principalExtractor.extractUserId(authentication);
        return ResponseEntity.ok(Map.of("point", memberService.getPoint(userId)));
    }
}
