package com.encore.encore.domain.user.controller;


import com.encore.encore.domain.user.dto.UserJoinRequestDto;
import com.encore.encore.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입 페이지 이동
    @GetMapping("/join")
    public String joinForm(Model model) {
        model.addAttribute("userJoinRequest", new UserJoinRequestDto());
        return "user/join"; // templates/user/join.html
    }

    // 회원가입 처리
    @PostMapping("/join")
    public String join(@ModelAttribute UserJoinRequestDto request) {
        userService.join(request);
        return "redirect:/login"; // 가입 성공 시 로그인 페이지로 이동
    }
}
