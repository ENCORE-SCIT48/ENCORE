package com.encore.encore.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 1. 로그인/로그아웃 설정을 먼저 정의
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/login") // 이제 시큐리티가 이 경로의 POST를 낚아챕니다.
                .usernameParameter("email")
                .defaultSuccessUrl("/profiles/select", true)
                .failureHandler((request, response, exception) -> {
                    // 콘솔창(IntelliJ 하단 Log)에 에러 원인이 찍힙니다.
                    System.out.println("### 로그인 실패 원인: " + exception.getMessage());
                    response.sendRedirect("/auth/login?error=true");
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/auth/login")
                .invalidateHttpSession(true)
            )

            // 2. 권한 설정은 마지막에 (가장 포괄적인 permitAll은 맨 아래)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()

                // 2. 인증(로그인)이 필수인 경로들 (UserDetails가 필요한 모든 곳)
                .requestMatchers("/profiles/**").authenticated()
                .requestMatchers("/setup/**").authenticated()
                .requestMatchers("/user/**").authenticated() // 유저 관련 일반 경로 추가
                .requestMatchers("/ws/**", "/ws").authenticated()
                // 3. 역할(Role)에 따른 제한
                // [기존 정보 참고] USER("관람객"), PERFORMER("공연자"), HOST("주최자")


                // 4. 나머지 모든 요청은 로그인 없이 허용 (개발 편의성)
                .anyRequest().permitAll()
            );

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
            .requestMatchers(
                "/favicon.ico",
                "/.well-known/**",
                "/css/**",        // CSS 허용
                "/js/**",         // JS 허용
                "/image/**",      // 기본 이미지 폴더 허용
                "/images/**",     // (로그에 images라고 찍혔으니 이것도 추가)
                "/uploads/**"     // ★ 업로드된 프로필 사진 경로 허용
            );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 비밀번호를 데이터베이스에 그대로 저장하면 절대 안 됩니다!
        // BCrypt라는 알고리즘으로 암호화해주는 빈을 등록합니다.
        return new BCryptPasswordEncoder();
    }

    // CORS 기본 설정: 모든 도메인 허용 (개발 단계용)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
