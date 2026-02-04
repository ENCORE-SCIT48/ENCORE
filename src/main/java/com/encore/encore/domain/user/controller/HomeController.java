package com.encore.encore.domain.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // @RestController가 아닌 것에 주의하세요! (HTML 반환용)
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "ENCORE PROJECT");
        model.addAttribute("message", "백엔드 서버가 성공적으로 실행되었습니다.");
        model.addAttribute("leader", "팀장님");
        return "index"; // index.html을 찾아서 띄웁니다.
    }
}
