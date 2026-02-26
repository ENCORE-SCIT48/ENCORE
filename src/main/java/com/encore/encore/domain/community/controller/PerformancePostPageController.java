package com.encore.encore.domain.community.controller;

import com.encore.encore.domain.community.dto.PerformancePostDto.ResponseListPerformancePostDto;
import com.encore.encore.domain.community.dto.PerformancePostDto.ResponseReadPerformancePostDto;
import com.encore.encore.domain.community.service.PerformancePostService;
import com.encore.encore.domain.community.service.PerformerRecommendationService;
import com.encore.encore.domain.community.service.PostInteractionService;
import com.encore.encore.domain.member.dto.ResponsePerformerRecommendDto;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.global.config.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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
    private final PostInteractionService postInteractionService;
    private final PerformerRecommendationService performerRecommendationService;

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
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(size = 5, sort = "createdAt") Pageable pageable,
            Model model) {

        log.info("GET /posts/performance - 목록 페이지 요청");

        Page<ResponseListPerformancePostDto> page = performancePostService.listPerformancePosts(keyword, pageable);

        model.addAttribute("posts", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);

        if (userDetails != null) {
            model.addAttribute("profileMode",
                    userDetails.getActiveMode().name());
        }

        return "community/performance/performancePost";
    }

    /**
     * [설명] 공연 모집 게시글 상세 화면을 조회합니다.
     *
     * - 게시글 단건 조회 (조회수 증가)
     * - 승인(APPROVED) 인원 수를 함께 조회합니다.
     * - 로그인 상태라면 활성 작성자 ID 및 신청 여부를 확인합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @param postId      조회할 게시글 ID
     * @param model       View 전달 객체
     * @return 공연 모집 게시글 상세 화면
     */
    @GetMapping("/{postId}")
    public String readPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("postId") Long postId,
            Model model) {

        log.info("[PerformanceDetail] 요청 - postId={}", postId);

        ResponseReadPerformancePostDto post = performancePostService.readPerformancePost(postId, true);

        model.addAttribute("post", post);
        model.addAttribute("approvedCount", post.getApprovedCount());

        if (userDetails != null) {

            Long activeAuthorId = performancePostService.getActiveAuthorId(userDetails);

            boolean alreadyApplied = postInteractionService.isAlreadyApplied(postId, userDetails);

            ActiveMode activeMode = userDetails.getActiveMode();

            model.addAttribute("activeAuthorId", activeAuthorId);
            model.addAttribute("alreadyApplied", alreadyApplied);
            model.addAttribute("profileMode", activeMode.name());

            log.debug("[PerformanceDetail] authorId={}, alreadyApplied={}, mode={}",
                    activeAuthorId,
                    alreadyApplied,
                    activeMode);
        }

        return "community/performance/performancePostDetail";
    }

    /**
     * [설명] 공연 모집 게시글 작성 화면을 조회합니다.
     *
     * - 로그인 사용자만 접근 가능합니다.
     * - 활성 모드가 ROLE_PERFORMER인 경우에만 접근 가능합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @param model       View 전달 객체
     * @return 공연 모집 게시글 작성 화면
     */
    @GetMapping("/write")
    public String writePostForm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        log.info("[PerformanceWrite] 요청");

        // 1. 로그인 체크
        if (userDetails == null) {
            log.warn("[PerformanceWrite] 비로그인 사용자 접근");
            return "redirect:/auth/login";
        }

        ActiveMode activeMode = userDetails.getActiveMode();

        // 2. 활성 모드 체크 (공연자만 허용)
        if (activeMode != ActiveMode.ROLE_PERFORMER) {
            log.warn("[PerformanceWrite] 공연자 모드 아님 - mode={}", activeMode);
            return "redirect:/posts/performance";
        }

        // 3. 모델 데이터 세팅
        model.addAttribute("nickname",
                userDetails.getUser().getNickname());
        model.addAttribute("profileMode",
                activeMode.name());

        log.debug("[PerformanceWrite] 접근 허용 - mode={}", activeMode);

        return "community/performance/performancePostWrite";
    }

    /**
     * [설명] 공연 모집 게시글 수정 화면을 조회합니다.
     *
     * - 로그인 사용자만 접근 가능합니다.
     * - 활성 모드 기준 작성자만 접근 가능합니다.
     * - 조회수는 증가시키지 않습니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @param postId      수정할 게시글 ID
     * @param model       View 전달 객체
     * @return 공연 모집 게시글 수정 화면
     */
    @GetMapping("/{postId}/edit")
    public String editPostForm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("postId") Long postId,
            Model model) {

        log.info("[PerformanceEdit] 요청 - postId={}", postId);

        if (userDetails == null) {
            log.warn("[PerformanceEdit] 비로그인 사용자 접근");
            return "redirect:/auth/login";
        }

        ResponseReadPerformancePostDto post = performancePostService.readPerformancePost(postId, false);

        Long activeAuthorId = performancePostService.getActiveAuthorId(userDetails);

        boolean isOwner = post.getPerformerId() != null &&
                post.getPerformerId().equals(activeAuthorId);

        if (!isOwner) {
            log.warn("[PerformanceEdit] 권한 없음 - postId={}, authorId={}",
                    postId,
                    activeAuthorId);
            return "redirect:/posts/performance";
        }

        model.addAttribute("post", post);

        return "community/performance/performancePostUpdate";
    }

    /**
     * [설명] 특정 공연 모집 게시글의 공연자 추천 화면을 조회합니다.
     *
     * - 로그인 사용자만 접근 가능합니다.
     * - 로그인한 공연자를 제외한 공연자 목록을 조회합니다.
     * - 무대명(keyword), 활동 지역(activityArea), 포지션(part) 조건을 적용합니다.
     * - Pageable을 이용하여 페이징 처리합니다.
     *
     * @param userDetails  로그인 사용자 정보
     * @param postId       공연 모집 게시글 ID
     * @param keyword      무대명 검색 키워드 (nullable)
     * @param activityArea 활동 지역 필터 (nullable)
     * @param part         포지션 필터 (nullable)
     * @param pageable     페이징 정보 (page, size)
     * @param model        View 전달 객체
     * @return 공연자 추천 화면
     */
    @GetMapping("/{postId}/recommend")
    public String recommendPerformers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("postId") Long postId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "activityArea", required = false) String activityArea,
            @RequestParam(value = "part", required = false) String part,
            @PageableDefault(size = 2) Pageable pageable,
            Model model) {

        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        Page<ResponsePerformerRecommendDto> performers = performerRecommendationService.getPerformerList(
                userDetails,
                keyword,
                activityArea,
                part,
                pageable);

        model.addAttribute("postId", postId);
        model.addAttribute("performers", performers.getContent());
        model.addAttribute("page", performers);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activityArea", activityArea);
        model.addAttribute("part", part);

        return "community/performance/performerRecommend";
    }
}