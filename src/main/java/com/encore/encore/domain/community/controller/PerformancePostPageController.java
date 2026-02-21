package com.encore.encore.domain.community.controller;

import com.encore.encore.domain.community.dto.PerformancePostDto.ResponseListPerformancePostDto;
import com.encore.encore.domain.community.dto.PerformancePostDto.ResponseReadPerformancePostDto;
import com.encore.encore.domain.community.service.PerformancePostService;
import com.encore.encore.global.config.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/posts/performance")
public class PerformancePostPageController {

    private final PerformancePostService performancePostService;

    /**
     * [설명] 공연 모집 게시글 목록 화면을 조회합니다.
     *
     * - 검색어가 존재하면 제목 기준 부분 검색을 수행합니다.
     * - 페이징 정보를 기반으로 게시글 목록을 조회합니다.
     * - 조회 결과를 model에 담아 뷰로 전달합니다.
     *
     * @param keyword  검색어 (nullable)
     * @param pageable 페이징 정보
     * @param model    View 전달 객체
     * @return 게시글 목록 화면
     */
    @GetMapping
    public String post(
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(size = 5, sort = "createdAt") Pageable pageable,
            Model model) {

        log.info("GET /posts/performance - 목록 페이지 요청");

        Page<ResponseListPerformancePostDto> page =
                performancePostService.listPerformancePosts(keyword, pageable);

        model.addAttribute("posts", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);

        return "community/performance/performancePost";
    }

    /**
     * [설명] 공연 모집 게시글 상세 화면을 조회합니다.
     *
     * - 게시글 단건 조회
     * - 로그인 상태라면 활성 작성자 ID(Host 또는 Performer)를 조회
     * - activeAuthorId를 model에 담아 버튼 제어에 사용합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @param postId      조회할 게시글 ID
     * @param model       View 전달 객체
     * @return 게시글 상세 화면
     */
    @GetMapping("/{postId}")
    public String readPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("postId") Long postId,
            Model model) {

        log.info("GET /posts/performance/{} - 상세 페이지 요청", postId);

        // 1. 게시글 조회
        ResponseReadPerformancePostDto post =
                performancePostService.readPerformancePost(postId);

        model.addAttribute("post", post);

        // 2. 로그인 사용자 활성 작성자 ID 조회
        if (userDetails != null) {

            Long activeAuthorId =
                    performancePostService.getActiveAuthorId(userDetails);

            log.info("activeAuthorId={}", activeAuthorId);

            model.addAttribute("activeAuthorId", activeAuthorId);
        }

        return "community/performance/performancePostDetail";
    }

    /**
     * [설명] 공연 모집 게시글 작성 화면을 조회합니다.
     *
     * - 로그인하지 않은 경우 로그인 페이지로 리다이렉트합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @return 게시글 작성 화면
     */
    @GetMapping("/write")
    public String writePostForm(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("GET /posts/performance/write - 작성 페이지 요청");

        if (userDetails == null) {
            log.info("비로그인 사용자 - 로그인 페이지로 리다이렉트");
            return "redirect:/auth/login";
        }

        return "community/performance/performancePostWrite";
    }

    /**
     * [설명] 공연 모집 게시글 수정 화면을 조회합니다.
     *
     * - 로그인 사용자만 접근 가능합니다.
     * - Host 또는 Performer 작성자인 경우에만 접근 허용합니다.
     * - 작성자가 아닌 경우 목록 페이지로 리다이렉트합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @param postId      수정할 게시글 ID
     * @param model       View 전달 객체
     * @return 게시글 수정 화면
     */
    @GetMapping("/{postId}/edit")
    public String editPostForm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("postId") Long postId,
            Model model) {

        log.info("GET /posts/performance/{}/edit - 수정 페이지 요청", postId);

        // 1. 로그인 체크
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        // 2. 게시글 조회
        ResponseReadPerformancePostDto post =
                performancePostService.readPerformancePost(postId);

        // 3. 활성 작성자 ID 조회
        Long activeAuthorId =
                performancePostService.getActiveAuthorId(userDetails);

        log.info("권한 체크 - activeAuthorId={}, hostId={}, performerId={}",
                activeAuthorId,
                post.getHostId(),
                post.getPerformerId());

        // 4. Host 또는 Performer 작성자 여부 확인
        boolean isHostOwner =
                post.getHostId() != null &&
                post.getHostId().equals(activeAuthorId);

        boolean isPerformerOwner =
                post.getPerformerId() != null &&
                post.getPerformerId().equals(activeAuthorId);

        if (!isHostOwner && !isPerformerOwner) {
            log.info("작성자 불일치 - 수정 페이지 접근 차단");
            return "redirect:/posts/performance";
        }

        model.addAttribute("post", post);

        return "community/performance/performancePostUpdate";
    }

    /**
     * [설명] 특정 공연 모집 게시글에 대한 추천 공연자 화면을 조회합니다.
     *
     * - 로그인 사용자만 접근 가능합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @param postId      공연 모집 게시글 ID
     * @param model       View 전달 객체
     * @return 추천 공연자 화면
     */
    @GetMapping("/{postId}/recommend")
    public String recommendPerformers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("postId") Long postId,
            Model model) {

        log.info("GET /posts/performance/{}/recommend - 추천 공연자 화면 요청", postId);

        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("postId", postId);

        return "community/performance/performerRecommend";
    }
}