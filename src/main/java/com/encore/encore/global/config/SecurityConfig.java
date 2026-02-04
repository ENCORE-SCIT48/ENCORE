package com.encore.encore.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
            // 1. CSRF 보호 비활성화 (개발 단계나 API 위주일 때 주로 끕니다. 운영 시에는 고려 필요)
            .csrf(csrf -> csrf.disable())

            // 2. 접근 권한 설정 (누가 어디에 들어올 수 있는지)
            .authorizeHttpRequests(auth -> auth
                // 회원가입, 메인 페이지, 정적 리소스(CSS/JS)는 누구나 접근 가능
                .requestMatchers("/", "/join", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                // 아까 언급한 Swagger 관련 경로도 열어줘야 합니다.
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 그 외 모든 요청은 로그인한 사용자만 접근 가능
                .anyRequest().authenticated()
            )

            // 3. 로그인 설정
            .formLogin(form -> form
                .loginPage("/login")           // 커스텀 로그인 페이지 경로
                .defaultSuccessUrl("/")        // 로그인 성공 시 이동할 페이지
                .usernameParameter("email")    // 엔티티에 email로 되어 있으므로 설정
                .permitAll()
            )

            // 4. 로그아웃 설정
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)   // 세션 무효화
            );

        return http.build();
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
