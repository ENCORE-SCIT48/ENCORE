package com.encore.encore.domain.community.controller;

import com.encore.encore.domain.community.dto.RequestCreatePostDto;
import com.encore.encore.domain.community.dto.ResponseCreatePostDto;
import com.encore.encore.domain.community.dto.ResponseDeletePostDto;
import com.encore.encore.domain.community.dto.ResponseListPostDto;
import com.encore.encore.domain.community.dto.ResponseReadPostDto;
import com.encore.encore.domain.community.service.PostService;
import com.encore.encore.global.common.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    /**
     * [설명] 게시글을 등록합니다.
     * 
     * @param request 게시글 등록 요청 객체
     * @return 게시글 등록 결과
     */
    @PostMapping
    public CommonResponse<ResponseCreatePostDto> create(
            @RequestBody RequestCreatePostDto request) {
        log.info("POST /api/posts - 게시글 등록 요청");

        ResponseCreatePostDto result = postService.createPost(request);

        log.info("POST /api/posts - 게시글 등록 완료, postId={}", result.getPostId());

        return CommonResponse.ok(result, "게시글이 정상적으로 등록되었습니다.");
    }

    /**
     * [설명] 게시글을 삭제합니다. (논리 삭제)
     * 
     * @param id 삭제할 게시글 ID
     * @return 게시글 삭제 결과
     */
    @DeleteMapping("/{id}")
    public CommonResponse<ResponseDeletePostDto> delete(
            @PathVariable("id") Long id) {

        log.error("### POST CONTROLLER DELETE CALLED ###");

        log.info("DELETE /api/posts/{} - 게시글 삭제 요청", id);

        ResponseDeletePostDto result = postService.deletePost(id);

        log.info("DELETE /api/posts/{} - 게시글 삭제 완료", id);

        return CommonResponse.ok(result, "게시글이 정상적으로 삭제되었습니다.");
    }

    /**
     * [설명] 공연자 모집 게시글 목록을 페이징 조회합니다.
     * postType이 전달되면 해당 타입의 게시글만 조회합니다.
     *
     * @param postType 게시글 타입 (선택)
     * @param pageable 페이징 정보
     * @return 게시글 목록 페이지
     */
    @GetMapping
    public CommonResponse<Page<ResponseListPostDto>> listPosts(
            @RequestParam(name = "postType", required = false) String postType,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        log.info(
                "GET /api/posts - 게시글 목록 조회 요청, postType={}, page={}, size={}",
                postType,
                pageable.getPageNumber(),
                pageable.getPageSize());

        Page<ResponseListPostDto> result = (postType == null || postType.isBlank())
                ? postService.listPosts(pageable)
                : postService.listPostsByType(postType, pageable);

        log.info(
                "GET /api/posts - 게시글 목록 조회 완료, totalElements={}",
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
    public CommonResponse<ResponseReadPostDto> readPost(
            @PathVariable("id") Long id) {
        log.info("GET /api/posts/{} - 게시글 단건 조회 요청", id);

        ResponseReadPostDto result = postService.readPost(id);

        log.info("GET /api/posts/{} - 게시글 단건 조회 완료", id);

        return CommonResponse.ok(result, "게시글 조회 성공");
    }
}
