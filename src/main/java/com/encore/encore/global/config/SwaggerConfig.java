package com.encore.encore.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.utils.SpringDocUtils; // 추가
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.encore.encore.global.config.CustomUserDetails; // 추가

@Configuration
public class SwaggerConfig {

    static {
        // [중요] @AuthenticationPrincipal로 들어오는 객체는 Swagger가 분석하지 않도록 설정
        SpringDocUtils.getConfig().addAnnotationsToIgnore(
            org.springframework.security.core.annotation.AuthenticationPrincipal.class,
            com.encore.encore.global.config.CustomUserDetails.class
        );
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Encore Project API")
                .description("공연 매칭 플랫폼 API 명세서")
                .version("v1.0.0"));
    }
}
