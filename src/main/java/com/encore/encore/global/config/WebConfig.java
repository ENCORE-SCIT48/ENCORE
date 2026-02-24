package com.encore.encore.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 외부 물리 파일 관련 요청 처리
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 브라우저에서 /uploads/** 로 시작하는 경로로 요청이 들어오면
        registry.addResourceHandler("/uploads/**")
            // 실제 물리적인 외부 폴더 경로와 연결합니다.
            // file:///C:/encore/uploads/ 이런 식으로 매핑됩니다.
            .addResourceLocations("file:///" + uploadDir);
    }
}
