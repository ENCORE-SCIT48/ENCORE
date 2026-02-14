package com.encore.encore.domain.community.controller;

import com.encore.encore.domain.community.dto.PerformerPostDto.*;
import com.encore.encore.domain.community.service.PerformerPostService;
import com.encore.encore.global.common.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/performer")
@RequiredArgsConstructor
@Slf4j
public class PerformerPostController {

    private final PerformerPostService performerPostService;

    /**
     * [설명] 공연자 모집 게시글을 등록합니다.
     *
     * @param request 게시글 등록 요청 객체
     * @return 게시글 등록 결과
     */
    @PostMapping
    public CommonResponse<ResponseCreatePerformerPostDto> create(
            @RequestBody RequestCreatePerformerPostDto request) {

        log.info("POST /api/posts/performer - 공연자 모집 게시글 등록 요청");

        ResponseCreatePerformerPostDto result = performerPostService.createPerformerPost(request);

        log.info(
                "POST /api/posts/performer - 공연자 모집 게시글 등록 완료, postId={}",
                result.getPostId());

        return CommonResponse.ok(result, "게시글이 정상적으로 등록되었습니다.");
    }

    /**
     * [설명] 공연자 모집 게시글을 삭제합니다. (논리 삭제)
     *
     * @param id 삭제할 게시글 ID
     * @return 게시글 삭제 결과
     */
    @DeleteMapping("/{id}")
    public CommonResponse<ResponseDeletePerformerPostDto> delete(
            @PathVariable("id") Long id) {

        log.info(
                "DELETE /api/posts/performer/{} - 공연자 모집 게시글 삭제 요청",
                id);

        ResponseDeletePerformerPostDto result = performerPostService.deletePerformerPost(id);

        log.info(
                "DELETE /api/posts/performer/{} - 공연자 모집 게시글 삭제 완료",
                id);

        return CommonResponse.ok(result, "게시글이 정상적으로 삭제되었습니다.");
    }

    /**
     * [설명] 공연자 모집 게시글을 수정합니다.
     *
     * @param id      게시글 ID
     * @param request 게시글 수정 요청 객체
     * @return 수정된 게시글 정보
     */
    @PutMapping("/{id}")
    public CommonResponse<ResponseUpdatePerformerPostDto> update(
            @PathVariable("id") Long id,
            @RequestBody RequestUpdatePerformerPostDto request) {
        log.info(
                "PUT /api/posts/performer/{} - 공연자 모집 게시글 수정 요청",
                id);

        ResponseUpdatePerformerPostDto result = performerPostService.updatePerformerPost(id, request);

        log.info(
                "PUT /api/posts/performer/{} - 공연자 모집 게시글 수정 완료",
                id);

        return CommonResponse.ok(result, "게시글이 정상적으로 수정되었습니다.");
    }

    /**
     * [설명] 공연자 모집 게시글 목록을 페이징 조회합니다.
     * 검색어(keyword)가 존재하면 제목 검색을 수행합니다.
     *
     * @param keyword  검색어
     * @param pageable 페이징 정보
     * @return 게시글 목록 페이지
     */
    @GetMapping
    public CommonResponse<Page<ResponseListPerformerPostDto>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        log.info(
                "GET /api/posts/performer - 공연자 모집 게시글 목록 조회, keyword={}, page={}, size={}",
                keyword,
                pageable.getPageNumber(),
                pageable.getPageSize());

        Page<ResponseListPerformerPostDto> result = performerPostService.listPerformerPosts(keyword, pageable);

        log.info(
                "GET /api/posts/performer - 공연자 모집 게시글 목록 조회 완료, totalElements={}",
                result.getTotalElements());

        return CommonResponse.ok(result, "게시글 목록 조회 성공");
    }

    /**
     * [설명] 공연자 모집 게시글 단건 상세 정보를 조회합니다.
     * 조회 시 조회수가 증가합니다.
     *
     * @param id 게시글 ID
     * @return 게시글 상세 정보
     */
    @GetMapping("/{id}")
    public CommonResponse<ResponseReadPerformerPostDto> read(
            @PathVariable("id") Long id) {

        log.info(
                "GET /api/posts/performer/{} - 공연자 모집 게시글 단건 조회 요청",
                id);

        ResponseReadPerformerPostDto result = performerPostService.readPerformerPost(id);

        log.info(
                "GET /api/posts/performer/{} - 공연자 모집 게시글 단건 조회 완료",
                id);

        return CommonResponse.ok(result, "게시글 조회 성공");
    }
}
