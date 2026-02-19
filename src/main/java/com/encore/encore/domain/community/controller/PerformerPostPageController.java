package com.encore.encore.domain.community.controller;

import com.encore.encore.domain.community.dto.PerformerPostDto.ResponseListPerformerPostDto;
import com.encore.encore.domain.community.dto.PerformerPostDto.ResponseReadPerformerPostDto;
import com.encore.encore.domain.community.service.PerformerPostService;
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

    /**
     * [설명] 공연자 모집 게시글 목록 화면을 조회합니다.
     *
     * - 검색어가 존재하면 검색 조건으로 조회합니다.
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

        log.info("GET /posts/performer - 목록 페이지 요청");

        // 게시글 목록 조회
        Page<ResponseListPerformerPostDto> page = performerPostService.listPerformerPosts(keyword, pageable);

        log.info("게시글 목록 조회 완료 - totalElements={}", page.getTotalElements());

        // 뷰 전달 데이터 세팅
        model.addAttribute("posts", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);

        return "community/performer/performerPost";
    }

    /**
     * [설명] 공연자 모집 게시글 상세 화면을 조회합니다.
     *
     * - 게시글 단건 조회
     * - 로그인 상태라면 PerformerProfile을 조회(없으면 생성)
     * - activeProfileId를 model에 담아 버튼 제어에 사용합니다.
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

        // 1. 게시글 조회
        ResponseReadPerformerPostDto post = performerPostService.readPerformerPost(postId);

        model.addAttribute("post", post);

        // 2. 로그인 사용자 프로필 ID 조회 (없으면 자동 생성)
        if (userDetails != null) {

            log.info("로그인 사용자 존재 - userId={}",
                    userDetails.getUser().getUserId());

            Long activeProfileId = performerPostService.getActivePerformerId(userDetails);

            log.info("activeProfileId 설정 - {}", activeProfileId);

            model.addAttribute("activeProfileId", activeProfileId);

        } else {
            log.info("비로그인 상태 - activeProfileId 없음");
        }

        return "community/performer/performerPostDetail";
    }

    /**
     * [설명] 공연자 모집 게시글 작성 화면을 조회합니다.
     *
     * - 로그인하지 않은 경우 로그인 페이지로 리다이렉트합니다.
     * - 로그인한 경우 작성 화면을 반환합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @return 게시글 작성 화면
     */
    @GetMapping("/write")
    public String writePostForm(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("GET /posts/performer/write - 작성 페이지 요청");

        if (userDetails == null) {
            log.info("비로그인 사용자 - 로그인 페이지로 리다이렉트");
            return "redirect:/auth/login";
        }

        log.info("로그인 사용자 작성 페이지 접근 허용 - userId={}",
                userDetails.getUser().getUserId());

        return "community/performer/performerPostWrite";
    }

    /**
     * [설명] 공연자 모집 게시글 수정 화면을 조회합니다.
     *
     * - 로그인 사용자만 접근 가능
     * - 작성자 본인인 경우에만 접근 허용
     * - 작성자가 아닌 경우 목록 페이지로 리다이렉트
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

        log.info("GET /posts/performer/{}/edit - 수정 페이지 요청", postId);

        // 1. 로그인 체크
        if (userDetails == null) {
            log.info("비로그인 사용자 - 로그인 페이지로 리다이렉트");
            return "redirect:/auth/login";
        }

        // 2. 게시글 조회
        ResponseReadPerformerPostDto post = performerPostService.readPerformerPost(postId);

        // 3. 로그인 사용자 프로필 ID 조회
        Long activeProfileId = performerPostService.getActivePerformerId(userDetails);

        log.info("수정 권한 체크 - activeProfileId={}, postPerformerId={}",
                activeProfileId, post.getPerformerId());

        // 4. 작성자 일치 여부 확인
        if (activeProfileId == null ||
                !activeProfileId.equals(post.getPerformerId())) {

            log.info("작성자 불일치 - 수정 페이지 접근 차단");
            return "redirect:/posts/performer";
        }

        log.info("작성자 확인 완료 - 수정 페이지 접근 허용");

        model.addAttribute("post", post);

        return "community/performer/performerPostUpdate";
    }
}
