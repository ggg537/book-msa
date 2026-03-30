package com.a.user.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 설정값
 * application.yml jwt.* 값 바인딩
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** Base64 인코딩된 시크릿 키 (32자 이상) */
    private String secretKey;

    /** AccessToken 만료시간 (ms) - 기본 1800000 = 30분 */
    private long accessTokenExpiration;

    /** RefreshToken 만료시간 (ms) - 기본 604800000 = 7일 */
    private long refreshTokenExpiration;
}
