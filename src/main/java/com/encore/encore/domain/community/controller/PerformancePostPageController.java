package com.encore.encore.domain.community.controller;

import com.encore.encore.domain.community.dto.PerformancePostDto.ResponseListPerformancePostDto;
import com.encore.encore.domain.community.dto.PerformancePostDto.ResponseReadPerformancePostDto;
import com.encore.encore.domain.community.service.PerformancePostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
     * 검색어(keyword)가 존재하면 제목 기준 부분 검색을 수행합니다.
     *
     * @param keyword  검색어 (nullable)
     * @param pageable 페이징 정보
     * @param model    뷰에 전달할 데이터 모델
     * @return 게시글 목록 화면
     */
    @GetMapping
    public String post(
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(size = 5, sort = "createdAt") Pageable pageable,
            Model model) {

        log.info(
                "공연 모집 게시글 목록 화면 요청 - keyword={}, page={}, size={}",
                keyword,
                pageable.getPageNumber(),
                pageable.getPageSize());

        Page<ResponseListPerformancePostDto> page = performancePostService.listPerformancePosts(keyword, pageable);

        model.addAttribute("posts", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);

        return "community/performance/performancePost";
    }

    /**
     * 공연 모집 게시글 상세 화면을 조회합니다.
     *
     * @param postId 조회할 게시글 ID
     * @param model  뷰에 전달할 데이터 모델
     * @return 게시글 상세 화면
     */
    @GetMapping("/{postId}")
    public String readPost(
            @PathVariable("postId") Long postId,
            Model model) {

        log.info("공연 모집 게시글 상세 화면 요청 - postId={}", postId);

        ResponseReadPerformancePostDto post = performancePostService.readPerformancePost(postId);

        model.addAttribute("post", post);

        return "community/performance/performancePostDetail";
    }

    /**
     * 공연 모집 게시글 작성 화면을 조회합니다.
     *
     * @return 게시글 작성 화면
     */
    @GetMapping("/write")
    public String writePostForm() {
        log.info("공연 모집 게시글 작성 화면 요청");
        return "community/performance/performancePostWrite";
    }

    /**
     * 공연 모집 게시글 수정 화면을 조회합니다.
     *
     * @param postId 수정할 게시글 ID
     * @param model  뷰에 전달할 데이터 모델
     * @return 게시글 수정 화면
     */
    @GetMapping("/{postId}/edit")
    public String editPostForm(
            @PathVariable("postId") Long postId,
            Model model) {

        log.info("공연 모집 게시글 수정 화면 요청 - postId={}", postId);

        ResponseReadPerformancePostDto post = performancePostService.readPerformancePost(postId);

        model.addAttribute("post", post);

        return "community/performance/performancePostUpdate";
    }

    /**
     * 특정 공연 모집 게시글에 대한 추천 공연자 화면을 조회합니다.
     *
     * @param postId 공연 모집 게시글 ID
     * @param model  뷰에 전달할 데이터 모델
     * @return 추천 공연자 화면
     */
    @GetMapping("/{postId}/recommend")
    public String recommendPerformers(
            @PathVariable("postId") Long postId,
            Model model) {

        log.info("추천 공연자 화면 요청 - postId={}", postId);

        model.addAttribute("postId", postId);

        return "community/performance/performerRecommend";
    }
}
