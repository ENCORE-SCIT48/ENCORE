package com.encore.encore.domain.community.service;

import com.encore.encore.domain.community.dto.RequestCreatePostDto;
import com.encore.encore.domain.community.dto.ResponseCreatePostDto;
import com.encore.encore.domain.community.dto.ResponseDeletePostDto;
import com.encore.encore.domain.community.entity.Post;
import com.encore.encore.domain.community.repository.PostRepository;
import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.performance.repository.PerformanceRepository;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final PerformanceRepository performanceRepository;

    /**
     * [설명] 공연자 모집 게시글을 등록합니다.
     *
     * @param dto 게시글 등록 요청 객체
     * @return 등록된 게시글 정보
     */
    public ResponseCreatePostDto createPost(RequestCreatePostDto dto) {
        log.info("게시글 등록 시작 - postType={}", dto.getPostType());

        Performance performance = null;
        if (dto.getPerformanceId() != null) {
            performance = performanceRepository.findById(dto.getPerformanceId())
                    .orElseThrow(() -> {
                        log.error("공연 정보 없음 - performanceId={}", dto.getPerformanceId());
                        return new ApiException(ErrorCode.NOT_FOUND, "공연 정보를 찾을 수 없습니다.");
                    });
        }

        Post post = Post.builder()
                .performance(performance)
                .postType(dto.getPostType())
                .title(dto.getTitle())
                .content(dto.getContent())
                .viewCount(0)
                .build();

        Post savedPost = postRepository.save(post);

        log.info("게시글 등록 완료 - postId={}", savedPost.getPostId());

        return ResponseCreatePostDto.builder()
                .postId(savedPost.getPostId())
                .postType(savedPost.getPostType())
                .title(savedPost.getTitle())
                .createdAt(savedPost.getCreatedAt())
                .build();
    }

    /**
     * [설명] 공연자 모집 게시글을 논리 삭제합니다.
     *
     * @param postId 삭제할 게시글 ID
     * @return 게시글 삭제 결과
     */
    public ResponseDeletePostDto deletePost(Long postId) {


        log.info("게시글 삭제 요청 - postId={}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("게시글 없음 - postId={}", postId);
                    return new ApiException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다.");
                });

        if (post.isDeleted()) {
            log.error("이미 삭제된 게시글 - postId={}", postId);
            throw new ApiException(ErrorCode.INVALID_REQUEST, "이미 삭제된 게시글입니다.");
        }

        /**
         * Why:
         * 물리 삭제는 데이터 복구 및 이력 관리가 불가능하므로
         * 서비스 정책상 논리 삭제를 사용한다.
         */
        post.delete();

        log.info("게시글 삭제 완료 - postId={}", postId);

        return ResponseDeletePostDto.builder()
                .postId(postId)
                .deleted(true)
                .build();
    }
}
