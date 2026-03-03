package com.encore.encore.domain.community.controller;

import com.encore.encore.domain.community.dto.PerformerPostDto.ResponseListPerformerPostDto;
import com.encore.encore.domain.community.dto.PerformerPostDto.ResponseReadPerformerPostDto;
import com.encore.encore.domain.community.service.PerformerPostService;
import com.encore.encore.domain.community.service.PostInteractionService;
import com.encore.encore.domain.member.entity.ActiveMode;
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
@RequestMapping("/posts/performer")
public class PerformerPostPageController {

    private final PerformerPostService performerPostService;
    private final PostInteractionService postInteractionService;

    /**
     * [설명] 공연자 모집 게시글 목록 화면을 조회합니다.
     *
     * - 검색어(keyword)가 존재하면 제목 기준 부분 검색을 수행합니다.
     * - 페이징 정보를 기반으로 게시글 목록을 조회합니다.
     * - 각 게시글에는 정원(capacity)과 승인 인원(approvedCount)이 포함됩니다.
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

        log.info("GET /posts/performer - 목록 페이지 요청");

        Page<ResponseListPerformerPostDto> page = performerPostService.listPerformerPosts(keyword, pageable);

        log.info("목록 조회 완료 - totalElements={}", page.getTotalElements());

        model.addAttribute("posts", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);

        return "community/performer/performerPost";
    }

    /**
     * [설명] 공연자 모집 게시글 상세 화면을 조회합니다.
     *
     * - 게시글 단건 조회 (조회수 증가)
     * - 승인(APPROVED) 상태 신청 인원 수를 조회합니다.
     * - 로그인 상태라면 활성 PerformerProfile ID 및 신청 여부를 확인합니다.
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

        log.info("GET /posts/performer/{} - 상세 페이지 요청", postId);

        // 1. 게시글 조회 (조회수 증가 true)
        ResponseReadPerformerPostDto post = performerPostService.readPerformerPost(postId, true);

        model.addAttribute("post", post);
        model.addAttribute("approvedCount", post.getApprovedCount());

        if (userDetails != null) {

            Long activeAuthorId = performerPostService.getActivePerformerId(userDetails);

            boolean alreadyApplied = postInteractionService.isAlreadyApplied(postId, userDetails);

            ActiveMode activeMode = userDetails.getActiveMode();

            model.addAttribute("activeAuthorId", activeAuthorId);
            model.addAttribute("alreadyApplied", alreadyApplied);
            model.addAttribute("profileMode", activeMode.name());

            log.debug("[PerformerDetail] authorId={}, alreadyApplied={}, mode={}",
                    activeAuthorId,
                    alreadyApplied,
                    activeMode);
        }

        return "community/performer/performerPostDetail";
    }

    /**
     * [설명] 공연자 모집 게시글 작성 화면을 조회합니다.
     *
     * - 로그인 사용자만 접근 가능합니다.
     * - 활성 모드가 ROLE_PERFORMER인 경우에만 접근 가능합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @param model       View 전달 객체
     * @return 공연자 모집 게시글 작성 화면
     */
    @GetMapping("/write")
    public String writePostForm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        log.info("[PerformerWrite] 요청");

        // 1. 로그인 체크
        if (userDetails == null) {
            log.warn("[PerformerWrite] 비로그인 사용자 접근");
            return "redirect:/auth/login";
        }

        ActiveMode activeMode = userDetails.getActiveMode();

        // 2. 공연자 모드 체크
        if (activeMode != ActiveMode.ROLE_PERFORMER) {
            log.warn("[PerformerWrite] 공연자 모드 아님 - mode={}", activeMode);
            return "redirect:/posts/performer";
        }

        model.addAttribute("nickname",
                userDetails.getUser().getNickname());
        model.addAttribute("profileMode",
                activeMode.name());

        return "community/performer/performerPostWrite";
    }

    /**
     * [설명] 공연자 모집 게시글 수정 화면을 조회합니다.
     *
     * - 로그인 사용자만 접근 가능합니다.
     * - 활성 모드 기준 작성자(PerformerProfile)만 접근 가능합니다.
     * - 조회수는 증가시키지 않습니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @param postId      수정할 게시글 ID
     * @param model       View 전달 객체
     * @return 공연자 모집 게시글 수정 화면
     */
    @GetMapping("/{postId}/edit")
    public String editPostForm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("postId") Long postId,
            Model model) {

        log.info("[PerformerEdit] 요청 - postId={}", postId);

        if (userDetails == null) {
            log.warn("[PerformerEdit] 비로그인 사용자 접근");
            return "redirect:/auth/login";
        }

        ResponseReadPerformerPostDto post = performerPostService.readPerformerPost(postId, false);

        Long activeAuthorId = performerPostService.getActivePerformerId(userDetails);

        boolean isOwner = post.getPerformerId() != null &&
                post.getPerformerId().equals(activeAuthorId);

        if (!isOwner) {
            log.warn("[PerformerEdit] 권한 없음 - postId={}, authorId={}",
                    postId,
                    activeAuthorId);
            return "redirect:/posts/performer";
        }

        model.addAttribute("post", post);

        return "community/performer/performerPostUpdate";
    }
}