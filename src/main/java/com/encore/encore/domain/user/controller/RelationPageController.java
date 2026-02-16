package com.encore.encore.domain.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RelationPageController {

    @GetMapping("/user/follow")
    public String follow(
        @RequestParam Long targetId,
        @RequestParam(required = false, defaultValue = "USER") String profileMode,
        @RequestParam(required = false, defaultValue = "following") String tab,
        Model model) {


        model.addAttribute("activeTab", tab);
        model.addAttribute("targetId", targetId);
        model.addAttribute("profileMode", profileMode);

        return "relation/follower-following";
    }

}
