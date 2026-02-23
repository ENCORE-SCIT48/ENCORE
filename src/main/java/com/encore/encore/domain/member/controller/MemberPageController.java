package com.encore.encore.domain.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller
@Slf4j
public class MemberPageController {

    /**
     * 개인 페이지로 이동한다.
     *
     * @return
     */
    @GetMapping("member/profile/{profileId}/{profileMode}")
    public String memberProfile(
        @PathVariable Long profileId,
        @PathVariable String profileMode,
        Model model
    ) {
        model.addAttribute("profileId", profileId);
        model.addAttribute("profileMode", profileMode);

        return "member/memberProfile";
    }
}
