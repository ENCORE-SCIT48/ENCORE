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
public class UserPageController {


    /**
     * 팔로잉, 팔로워 리스트 페이지 이동
     *
     * @param targetId    팔로잉, 팔로워 리스트를 조회할 대상의 프로필 아이디
     * @param profileMode 팔로잉, 팔로워 리스트를 조회할 대상의 프로필 모드
     * @param tab         먼저 조회가 될 tab(following/follower)
     * @param model       데이터 저장
     * @return relation/following-follower.html
     */
    @GetMapping("/user/follow")
    public String follow(
        @RequestParam Long targetId,
        @RequestParam(required = false, defaultValue = "USER") String profileMode,
        @RequestParam(required = false, defaultValue = "following") String tab,
        Model model) {


        model.addAttribute("activeTab", tab);
        model.addAttribute("targetId", targetId);
        model.addAttribute("profileMode", profileMode);

        return "relation/following-follower";
    }

    /**
     * 알림 설정 페이지로 이동한다.
     *
     * @return
     */
    @GetMapping("user/notification")
    public String notification() {
        return "user-settings/notificationSetting";
    }

    @GetMapping("/user/block")
    public String blockList() {

        return "relation/blockList";
    }
}
