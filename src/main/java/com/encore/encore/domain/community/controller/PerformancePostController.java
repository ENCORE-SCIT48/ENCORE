package com.encore.encore.domain.community.controller;

import com.encore.encore.domain.community.dto.PerformancePostDto.*;
import com.encore.encore.domain.community.service.PerformancePostService;
import com.encore.encore.global.common.CommonResponse;
import com.encore.encore.global.config.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/performance")
@RequiredArgsConstructor
@Slf4j
public class PerformancePostController {

    private final PerformancePostService performancePostService;

    /**
     * [설명] 공연 모집 게시글을 등록합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @param request     게시글 등록 요청 객체
     * @return 게시글 등록 결과
     */
    @PostMapping
    public CommonResponse<ResponseCreatePerformancePostDto> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RequestCreatePerformancePostDto request) {

        log.info("POST /api/posts/performance - 공연 모집 게시글 등록 요청");

        ResponseCreatePerformancePostDto result = performancePostService.createPerformancePost(request, userDetails);

        log.info(
                "POST /api/posts/performance - 공연 모집 게시글 등록 완료, postId={}",
                result.getPostId());

        return CommonResponse.ok(result, "게시글이 정상적으로 등록되었습니다.");
    }

    /**
     * [설명] 공연 모집 게시글을 삭제합니다. (논리 삭제)
     *
     * @param userDetails 로그인 사용자 정보
     * @param id          삭제할 게시글 ID
     * @return 게시글 삭제 결과
     */
    @DeleteMapping("/{id}")
    public CommonResponse<ResponseDeletePerformancePostDto> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long id) {

        log.info(
                "DELETE /api/posts/performance/{} - 공연 모집 게시글 삭제 요청",
                id);

        ResponseDeletePerformancePostDto result = performancePostService.deletePerformancePost(id, userDetails);

        log.info(
                "DELETE /api/posts/performance/{} - 공연 모집 게시글 삭제 완료",
                id);

        return CommonResponse.ok(result, "게시글이 정상적으로 삭제되었습니다.");
    }

    /**
     * [설명] 공연 모집 게시글을 수정합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @param id          게시글 ID
     * @param request     게시글 수정 요청 객체
     * @return 수정된 게시글 정보
     */
    @PutMapping("/{id}")
    public CommonResponse<ResponseUpdatePerformancePostDto> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long id,
            @RequestBody RequestUpdatePerformancePostDto request) {

        log.info(
                "PUT /api/posts/performance/{} - 공연 모집 게시글 수정 요청",
                id);

        ResponseUpdatePerformancePostDto result = performancePostService.updatePerformancePost(id, request,
                userDetails);

        log.info(
                "PUT /api/posts/performance/{} - 공연 모집 게시글 수정 완료",
                id);

        return CommonResponse.ok(result, "게시글이 정상적으로 수정되었습니다.");
    }

    /**
     * [설명] 공연 모집 게시글 목록을 페이징 조회합니다.
     * 검색어(keyword)가 존재하면 제목 기준 부분 검색을 수행합니다.
     *
     * @param keyword  검색어
     * @param pageable 페이징 정보
     * @return 게시글 목록 페이지
     */
    @GetMapping
    public CommonResponse<Page<ResponseListPerformancePostDto>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        log.info(
                "GET /api/posts/performance - 공연 모집 게시글 목록 조회, keyword={}, page={}, size={}",
                keyword,
                pageable.getPageNumber(),
                pageable.getPageSize());

        Page<ResponseListPerformancePostDto> result = performancePostService.listPerformancePosts(keyword, pageable);

        log.info(
                "GET /api/posts/performance - 공연 모집 게시글 목록 조회 완료, totalElements={}",
                result.getTotalElements());

        return CommonResponse.ok(result, "게시글 목록 조회 성공");
    }

    /**
     * [설명] 공연 모집 게시글 단건 상세 정보를 조회합니다.
     * 조회 시 조회수가 증가합니다.
     *
     * @param id 게시글 ID
     * @return 게시글 상세 정보
     */
    @GetMapping("/{id}")
    public CommonResponse<ResponseReadPerformancePostDto> read(
            @PathVariable("id") Long id) {

        log.info(
                "GET /api/posts/performance/{} - 공연 모집 게시글 단건 조회 요청",
                id);

        ResponseReadPerformancePostDto result = performancePostService.readPerformancePost(id);

        log.info(
                "GET /api/posts/performance/{} - 공연 모집 게시글 단건 조회 완료",
                id);

        return CommonResponse.ok(result, "게시글 조회 성공");
    }
}