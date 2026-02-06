package com.encore.encore.domain.community.controller;

import com.encore.encore.domain.community.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/posts")
public class PostPageController {

    private final PostService postService;

    /**
     * [설명] 공연자 모집 게시글 목록 화면을 조회합니다.
     */
    @GetMapping()
    public String post(
            @RequestParam(name = "postType", required = false) String postType,
            Model model
    ) {
        log.info("게시글 목록 화면 요청 - postType={}", postType);

        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<?> posts = (postType == null || postType.isBlank())
                ? postService.listPosts(pageRequest)
                : postService.listPostsByType(postType, pageRequest);

        model.addAttribute("posts", posts.getContent());

        return "post";
    }

    /**
     * [설명] 공연자 모집 게시글 상세 화면을 조회합니다.
     */
    @GetMapping("{postId}")
    public String readPost(
            @PathVariable(name = "postId") Long postId,
            Model model
    ) {
        log.info("게시글 상세 화면 요청 - postId={}", postId);

        model.addAttribute("post", postService.readPost(postId));

        return "postDetail";
    }
}
