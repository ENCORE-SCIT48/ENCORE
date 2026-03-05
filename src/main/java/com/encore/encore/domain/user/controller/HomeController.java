package com.encore.encore.domain.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 기본 홈(/)은 피드로 사용.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/feed";
    }
}
