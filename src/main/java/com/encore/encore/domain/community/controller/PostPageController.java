package com.encore.encore.domain.community.controller;

import com.encore.encore.domain.community.dto.PerformerPostDto.ResponseListPerformerPostDto;
import com.encore.encore.domain.community.dto.PerformerPostDto.ResponseReadPerformerPostDto;
import com.encore.encore.domain.community.service.PerformerPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/posts/performer")
public class PostPageController {

    private final PerformerPostService performerPostService;

    /**
     * 공연자 모집 게시글 목록 화면을 조회합니다.
     *
     * @param model 뷰에 전달할 데이터 모델
     * @return 게시글 목록 화면
     */
    @GetMapping
    public String post(Model model) {
        log.info("공연자 모집 게시글 목록 화면 요청");

        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<ResponseListPerformerPostDto> posts =
                performerPostService.listPerformerPosts(pageRequest);

        model.addAttribute("posts", posts.getContent());

        return "community/performer/performerPost";
    }

    /**
     * 공연자 모집 게시글 상세 화면을 조회합니다.
     *
     * @param postId 조회할 게시글 ID
     * @param model  뷰에 전달할 데이터 모델
     * @return 게시글 상세 화면
     */
    @GetMapping("/{postId}")
    public String readPost(
            @PathVariable Long postId,
            Model model) {

        log.info("공연자 모집 게시글 상세 화면 요청 - postId={}", postId);

        ResponseReadPerformerPostDto post =
                performerPostService.readPerformerPost(postId);

        model.addAttribute("post", post);

        return "community/performer/performerPostDetail";
    }

    /**
     * 공연자 모집 게시글 작성 화면을 조회합니다.
     *
     * @return 게시글 작성 화면
     */
    @GetMapping("/write")
    public String writePostForm() {
        log.info("공연자 모집 게시글 작성 화면 요청");
        return "community/performer/performerPostWrite";
    }

    /**
     * 공연자 모집 게시글 수정 화면을 조회합니다.
     *
     * @param postId 수정할 게시글 ID
     * @param model  뷰에 전달할 데이터 모델
     * @return 게시글 수정 화면
     */
    @GetMapping("/{postId}/edit")
    public String editPostForm(
            @PathVariable Long postId,
            Model model) {

        log.info("공연자 모집 게시글 수정 화면 요청 - postId={}", postId);

        ResponseReadPerformerPostDto post =
                performerPostService.readPerformerPost(postId);

        model.addAttribute("post", post);

        return "community/performer/performerPostUpdate";
    }
}
