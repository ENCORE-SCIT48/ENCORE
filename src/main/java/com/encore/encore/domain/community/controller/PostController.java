package com.encore.encore.domain.community.controller;

import com.encore.encore.domain.community.dto.RequestCreatePostDto;
import com.encore.encore.domain.community.dto.ResponseCreatePostDto;
import com.encore.encore.domain.community.dto.ResponseDeletePostDto;
import com.encore.encore.domain.community.service.PostService;
import com.encore.encore.global.common.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
