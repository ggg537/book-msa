package com.a.user.security.config;

import com.a.user.security.details.CustomUserDetailsService;
import com.a.user.security.entrypoint.CustomAuthenticationEntryPoint;
import com.a.user.security.filter.CustomAuthenticationFilter;
import com.a.user.security.filter.JwtAuthenticationFilter;
import com.a.user.security.handler.CustomAuthenticationFailureHandler;
import com.a.user.security.handler.CustomAuthenticationSuccessHandler;
import com.a.user.security.handler.CustomLogoutSuccessHandler;
import com.a.user.security.handler.JwtCookieLogoutHandler;
import com.a.user.security.jwt.JwtAccessDeniedHandler;
import com.a.user.security.jwt.JwtCookieResolver;
import com.a.user.security.jwt.JwtTokenProvider;
import com.a.user.security.path.PermitAllPath;
import com.a.user.security.redis.RedisTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 전체 설정
 *
 * 역할:
 *   필터 체인 구성
 *   인증/인가 규칙 설정
 *   핸들러 등록
 *   로그아웃 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider                   jwtTokenProvider;
    private final JwtCookieResolver                  jwtCookieResolver;
    private final RedisTokenRepository               redisTokenRepository;
    private final CustomUserDetailsService           customUserDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomAuthenticationFailureHandler failureHandler;
    private final CustomLogoutSuccessHandler         logoutSuccessHandler;
    private final JwtCookieLogoutHandler             jwtCookieLogoutHandler;
    private final CustomAuthenticationEntryPoint     authenticationEntryPoint;
    private final JwtAccessDeniedHandler             accessDeniedHandler;
    private final ObjectMapper                       objectMapper;

    // BCrypt 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager 빈 등록
    // CustomAuthenticationFilter 에서 사용
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용)
                .csrf(csrf -> csrf.disable())

                // Form 로그인 비활성화
                .formLogin(formLogin -> formLogin.disable())

                // HTTP Basic 비활성화
                .httpBasic(httpBasic -> httpBasic.disable())

                // 세션 사용 안 함 (JWT Stateless)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 요청 권한 설정
                .authorizeHttpRequests(auth -> auth

                        // 인증 없이 접근 가능한 경로
                        .requestMatchers(PermitAllPath.PERMIT_ALL_PATHS).permitAll()

                        // 관리자만 접근 가능
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                // 예외 처리
                // CustomAuthenticationEntryPoint → 401 (인증 안 됨)
                // JwtAccessDeniedHandler         → 403 (권한 없음)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .addLogoutHandler(jwtCookieLogoutHandler)         // 쿠키 삭제
                        .logoutSuccessHandler(logoutSuccessHandler)        // Redis 삭제 + 응답
                        .permitAll()
                )

                // JWT 인증 필터 등록
                // 매 요청마다 토큰 검증
                .addFilterBefore(
                        jwtAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class
                )

                // 로그인 필터 등록
                // POST /api/auth/login 처리
                .addFilterBefore(
                        customAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * JWT 인증 필터 빈 생성
     * 매 API 요청마다 AccessToken 검증
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(
                jwtTokenProvider,
                jwtCookieResolver,
                redisTokenRepository,
                customUserDetailsService
        );
    }

    /**
     * 로그인 필터 빈 생성
     * POST /api/auth/login 요청 처리
     * 성공/실패 핸들러 연결
     */
    @Bean
    public CustomAuthenticationFilter customAuthenticationFilter()
            throws Exception {
        CustomAuthenticationFilter filter = new CustomAuthenticationFilter(
                authenticationManager(null),
                objectMapper
        );

        // 성공/실패 핸들러 직접 연결 (Facade 제거)
        filter.setAuthenticationSuccessHandler(successHandler);
        filter.setAuthenticationFailureHandler(failureHandler);

        return filter;
    }
}
