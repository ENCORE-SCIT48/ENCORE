package com.encore.encore.domain.user.controller;


import com.encore.encore.domain.user.dto.UserJoinRequestDto;
import com.encore.encore.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;

    /**
     * 회원가입 페이지 이동
     * @param model
     * @return 로그인 페이지로 경로
     */
    @GetMapping("/join")
    public String joinForm(Model model) {
        log.info("GET /user/join : 회원가입 페이지 요청");

        model.addAttribute("userJoinRequest", new UserJoinRequestDto());
        return "auth/join"; // templates/auth/join.html
    }

    /**
     * 회원가입 요청을 처리하고 로그인 페이지로 리다이렉트합니다.
     * @param userJoinRequestDto 회원가입 정보(이메일, 비밀번호, 닉네임 등)가 담긴 DTO
     * @return 로그인 페이지로의 redirect 경로
     */
    @PostMapping("/join")
    public String join(@Valid UserJoinRequestDto userJoinRequestDto,
                       Model model) {

        log.info("POST /user/join : 회원가입 시도 [Email: {}]", userJoinRequestDto.getEmail());

        String email = userService.join(userJoinRequestDto);

        model.addAttribute("userJoinRequest", email);

        return "redirect:/auth/login"; // 가입 성공 시 로그인 페이지로 이동
    }

    /**
     * 로그인 화면 이동
     * @return 로그인 화면
     */
    @GetMapping("/login")
    public String loginPage() {
        log.info("GET /user/login : 로그인 페이지 이동");
        return "auth/login"; // login.html
    }
}
