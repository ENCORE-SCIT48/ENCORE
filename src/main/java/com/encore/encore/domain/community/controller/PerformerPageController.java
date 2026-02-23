package com.encore.encore.domain.community.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.encore.encore.domain.community.dto.PerformerPostDto.ResponseListPerformerPostDto;
import com.encore.encore.domain.community.service.PerformerMypageService;
import com.encore.encore.global.config.CustomUserDetails;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/mypage/performer")
public class PerformerPageController {

    private final PerformerMypageService performerMypageService;

    /**
     * [설명] 로그인 공연자가 작성한 공연자 모집글 목록 화면을 조회합니다.
     *
     * - 로그인 사용자의 PerformerProfile을 기준으로 게시글을 조회합니다.
     * - postType이 PERFORMER_RECRUIT인 게시글만 조회합니다.
     * - 논리 삭제되지 않은 게시글만 조회합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @param pageable    페이징 정보
     * @param model       화면 전달 객체
     * @return 내가 작성한 공연자 모집글 목록 화면
     */
    @GetMapping("/posts")
    public String myPerformerPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable,
            Model model) {

        Page<ResponseListPerformerPostDto> page = performerMypageService.findMyPerformerPosts(userDetails, pageable);

        model.addAttribute("page", page);

        log.info("[PerformerPageController] 내가 작성한 공연자 모집글 화면 요청 - userId={}",
                userDetails.getUser().getUserId());

        return "community/mypage/performer/posts";
    }

    /**
     * [설명] 로그인 공연자가 작성한 공연 모집글 목록 화면을 조회합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @param pageable    페이징 정보
     * @param model       화면 전달 객체
     * @return 내가 작성한 공연 모집글 목록 화면
     */
    @GetMapping("/performances")
    public String myPerformancePosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable,
            Model model) {

        Page<ResponseListPerformerPostDto> page = performerMypageService.findMyPerformancePosts(userDetails, pageable);

        model.addAttribute("page", page);

        log.info("[PerformerPageController] 내가 작성한 공연 모집글 화면 요청 - userId={}",
                userDetails.getUser().getUserId());

        return "community/mypage/performer/performances";
    }

    /**
     * [설명] 로그인 공연자가 신청한 공연자 모집글 목록 화면을 조회합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @param model       화면 전달 객체
     * @return 신청한 공연자 모집글 목록 화면
     */
    @GetMapping("/applied-posts")
    public String appliedPerformerPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        List<ResponseListPerformerPostDto> list = performerMypageService.findAppliedPerformerPosts(userDetails);

        model.addAttribute("posts", list);

        return "community/mypage/performer/appliedPosts";
    }

    /**
     * [설명] 로그인 공연자가 신청한 공연 모집글 목록 화면을 조회합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @param model       화면 전달 객체
     * @return 신청한 공연 모집글 목록 화면
     */
    @GetMapping("/applied-performances")
    public String appliedPerformances(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        List<ResponseListPerformerPostDto> list = performerMypageService.findAppliedPerformances(userDetails);

        model.addAttribute("posts", list);

        log.info("[PerformerPageController] 내가 신청한 공연 화면 요청 - userId={}",
                userDetails.getUser().getUserId());

        return "community/mypage/performer/appliedPerformances";
    }

    /**
     * [설명] 모집 완료된 공연 리스트 화면을 조회합니다.
     *
     * @return 모집 완료 공연 목록 화면
     */
    @GetMapping("/completed-performances")
    public String completedPerformances() {

        log.info("[PerformerPageController] 모집 완료된 공연 리스트 화면 요청");

        return "community/mypage/performer/completedPerformances";
    }

    /**
     * [설명] 내가 모집한 공연자 화면을 조회합니다.
     *
     * @return 모집한 공연자 목록 화면
     */
    @GetMapping("/recruited-performers")
    public String recruitedPerformers() {

        log.info("[PerformerPageController] 내가 모집한 공연자 화면 요청");

        return "community/mypage/performer/recruitedPerformers";
    }

    /**
     * [설명] 내가 대관한 공연장 화면을 조회합니다.
     *
     * @return 대관한 공연장 목록 화면
     */
    @GetMapping("/venues")
    public String venueList() {

        log.info("[PerformerPageController] 내가 대관한 공연장 화면 요청");

        return "community/mypage/performer/venues";
    }
    
}