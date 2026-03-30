package com.a.user.security.handler;

import com.a.user.domain.Member;
import com.a.user.domain.RefreshToken;
import com.a.user.repository.MemberRepository;
import com.a.user.repository.RefreshTokenRepository;
import com.a.user.security.jwt.JwtCookieWriter;
import com.a.user.security.jwt.JwtTokenProvider;
import com.a.user.security.redis.RedisTokenRepository;
import com.a.user.security.token.CustomAuthenticationToken;
import com.a.user.security.util.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final JwtTokenProvider        jwtTokenProvider;
    private final JwtCookieWriter         jwtCookieWriter;
    private final RedisTokenRepository    redisTokenRepository;
    private final MemberRepository        memberRepository;
    private final RefreshTokenRepository  refreshTokenRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        // ① authentication 에서 userId, email 꺼냄
        CustomAuthenticationToken token =
                (CustomAuthenticationToken) authentication;
        Long   userId = token.getUserId();
        String email  = token.getEmail();

        // ② AccessToken 생성 (만료: 30분)
        String accessToken  = jwtTokenProvider.generateAccessToken(authentication);

        // ③ RefreshToken 생성 (만료: 7일)
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // ④ AccessToken 쿠키 저장
        jwtCookieWriter.writeAccessToken(response, accessToken);

        // ⑤ RefreshToken 쿠키 저장
        jwtCookieWriter.writeRefreshToken(response, refreshToken);

        // ⑥ RefreshToken Redis 저장 (빠른 검증용)
        long refreshExpiration = jwtTokenProvider.getExpiration(refreshToken);
        redisTokenRepository.saveRefreshToken(userId, refreshToken, refreshExpiration);

        // ⑦ RefreshToken DB 저장 (영구 보관)
        // 기존 토큰 있으면 갱신, 없으면 새로 생성
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(refreshExpiration / 1000);

        Optional<RefreshToken> existing =
                refreshTokenRepository.findByMemberId(userId);

        if (existing.isPresent()) {
            // 기존 토큰 갱신
            existing.get().updateToken(refreshToken, expiresAt);
        } else {
            // 새로 생성
            refreshTokenRepository.save(
                    RefreshToken.of(userId, refreshToken, expiresAt)
            );
        }

        // ⑧ 마지막 로그인 시간 업데이트
        memberRepository.findById(userId)
                .ifPresent(Member::updateLastLoginAt);

        log.info("로그인 성공 - userId: {}, email: {}, ip: {}",
                userId, email, WebUtil.getClientIp(request));

        // ⑨ 성공 응답
        WebUtil.writeJsonResponse(response, HttpStatus.OK.value(),
                Map.of(
                        "message", "로그인 성공",
                        "userId",  userId,
                        "email",   email
                )
        );
    }
}
