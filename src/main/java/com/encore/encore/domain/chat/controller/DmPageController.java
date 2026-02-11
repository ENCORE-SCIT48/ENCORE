package com.encore.encore.domain.chat.controller;

import com.encore.encore.global.config.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
@Slf4j
public class DmPageController {

    @GetMapping("dm/list")
    public String dmList(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return "dm/dm";
    }
}
