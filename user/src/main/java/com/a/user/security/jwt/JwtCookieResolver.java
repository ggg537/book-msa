package com.a.user.security.jwt;

import com.a.user.security.util.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtCookieResolver {

    private final JwtCookieProperties jwtCookieProperties;

    public Optional<String> resolveAccessToken(HttpServletRequest request) {
        return WebUtil.extractCookieValue(
                request,
                jwtCookieProperties.accessTokenName()
        );
    }

    public Optional<String> resolveRefreshToken(HttpServletRequest request) {
        return WebUtil.extractCookieValue(
                request,
                jwtCookieProperties.refreshTokenName()
        );
    }
}
