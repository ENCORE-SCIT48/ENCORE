package com.encore.encore.domain.community.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/performer")
public class PerformerPageController {

    /**
     * 공연자 공연장 리스트 화면 조회
     * 
     * @return 공연장 리스트 화면
     */
    @GetMapping("/venueList")
    public String venueList() {

        log.info("[PerformerPageController] 공연자 공연장 리스트 화면 요청");

        return "community/venueList";
    }

    /**
     * 공연자 마이페이지 화면 조회
     * 
     * @return 공연자 마이페이지 화면
     */
    @GetMapping("/mypage")
    public String mypage() {

        log.info("[PerformerPageController] 공연자 마이페이지 화면 요청");

        return "community/mypage";
    }

}
