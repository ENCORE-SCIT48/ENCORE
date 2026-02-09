package com.encore.encore.domain.performance.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/performances")
public class PerformancePageController {

    /**
     * 공연 목록 페이지 반환
     * @return 공연 목록 화면 템플릿 경로
     */
    @GetMapping
    public String listPage() {
        log.info("[PerformancePage] list page requested");
        return "performance/list";
    }

    /**
     * 공연 상세 페이지 반환 (데이터는 JS에서 /api/performances/{id} 호출로 조회)
     * @param performanceId 공연 ID
     * @param model Thymeleaf 모델(화면에 performanceId 전달)
     * @return 공연 상세 화면 템플릿 경로
     */
    @GetMapping("/{performanceId}")
    public String detailPage(@PathVariable Long performanceId, Model model) {
        log.info("[PerformancePage] detail page requested - performanceId={}", performanceId);

        model.addAttribute("performanceId", performanceId);
        return "performance/detail";
    }
}
