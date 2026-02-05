package com.encore.encore.domain.chat.service;

import com.encore.encore.domain.chat.dto.ChatPostCreateRequestDto;
import com.encore.encore.domain.chat.dto.ChatPostDetailResponseDto;
import com.encore.encore.domain.chat.dto.ChatPostListResponseDto;
import com.encore.encore.domain.chat.dto.ChatPostUpdateRequestDto;
import com.encore.encore.domain.chat.entity.ChatPost;
import com.encore.encore.domain.chat.entity.ChatRoom;
import com.encore.encore.domain.chat.repository.ChatPostRepository;
import com.encore.encore.domain.chat.repository.ChatRoomRepository;
import com.encore.encore.domain.member.repository.HostProfileRepository;
import com.encore.encore.domain.member.repository.PerformerProfileRepository;
import com.encore.encore.domain.member.repository.UserProfileRepository;
import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.performance.repsitory.PerformanceRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final ChatPostRepository chatPostRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final PerformanceRepository performanceRepository;
    private final HostProfileRepository hostProfileRepository;
    private final PerformerProfileRepository performerProfileRepository;
    private final UserProfileRepository userProfileRepository;

    /**
     * 글과 채팅방 논리 삭제
     *
     * @param postId
     */
    public void softDeletePost(Long postId, Long userId) {
        log.info("게시글 논리 삭제 시작 - postId: {}", postId);
        try {
            ChatPost chatPost = chatPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("글이 조회되지 않습니다. ID: " + postId));
            log.info("권한 확인");
            checkChatPostAuthority(chatPost, userId);
            chatPost.setDeleted(true);

            ChatRoom chatRoom = chatRoomRepository.findByChatPostId(postId);
            if (chatRoom != null) {
                chatRoom.setDeleted(true);
                log.info("채팅방 논리 삭제 완료 - chatRoomId: {}", chatRoom.getRoomId());
            }
            log.info("게시글 논리 삭제 완료 - postId: {}", postId);
        } catch (Exception e) {
            log.error("게시글 논리 삭제 중 에러 발생 - postId: {}", postId, e);
            throw e;
        }
    }

    /**
     * 작성글을 DB에 저장하고 채팅방 생성
     *
     * @param dto
     */
    public void createChatPostAndRoom(ChatPostCreateRequestDto dto) {
        log.info("채팅 게시글 및 방 생성 프로세스 시작 - 제목: {}", dto.getTitle());

        try {
            ChatPost.ChatPostBuilder builder = ChatPost.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .maxMember(dto.getMaxMember())
                .currentMember(0)
                .status(ChatPost.Status.OPEN);

            // 프로필 세팅 및 로그 기록
            if (dto.getHostId() != null) {
                log.info("작성자 타입: Host (ID: {})", dto.getHostId());
                builder.host(hostProfileRepository.findById(dto.getHostId())
                    .orElseThrow(() -> new EntityNotFoundException("HostProfile 없음: " + dto.getHostId())));
            } else if (dto.getProfileId() != null) {
                log.info("작성자 타입: User (ID: {})", dto.getProfileId());
                builder.profile(userProfileRepository.findById(dto.getProfileId())
                    .orElseThrow(() -> new EntityNotFoundException("UserProfile 없음: " + dto.getProfileId())));
            } else if (dto.getPerformerId() != null) {
                log.info("작성자 타입: Performer (ID: {})", dto.getPerformerId());
                builder.performer(performerProfileRepository.findById(dto.getPerformerId())
                    .orElseThrow(() -> new EntityNotFoundException("PerformerProfile 없음: " + dto.getPerformerId())));
            } else {
                log.error("채팅방 생성 실패: 작성자 프로필 정보가 모두 비어있음");
                throw new IllegalArgumentException("Host, User, Performer 중 하나는 반드시 필요합니다.");
            }

            ChatPost chatPost = builder.build();
            chatPost.addParticipant(); // 인원수 0 -> 1 증가
            chatPostRepository.save(chatPost);
            log.info("ChatPost 저장 성공 - 생성된 PostID: {}", chatPost.getId());

            ChatRoom chatRoom = ChatRoom.builder()
                .chatPost(chatPost)
                .roomType(ChatRoom.RoomType.CHAT)
                .build();

            chatRoomRepository.save(chatRoom);
            log.info("ChatRoom 생성 성공 - 생성된 RoomID: {}", chatRoom.getRoomId());

        } catch (Exception e) {
            log.error("채팅방 생성 중 서버 오류 발생", e);
            throw e;
        }
    }

    /**
     * 공연별 채팅방 리스트 출력
     *
     * @param performanceId
     * @return
     */
    public List<ChatPostListResponseDto> getChatPostsByPerformance(Long performanceId) {
        List<ChatPost> chatPostList = chatPostRepository.findByPerformance_PerformanceId(performanceId);
        List<ChatPostListResponseDto> dtoList = new ArrayList<>();

        for (ChatPost chatPost : chatPostList) {
            ChatPostListResponseDto dto = ChatPostListResponseDto.builder()
                .id(chatPost.getId())
                .currentMember(chatPost.getCurrentMember())
                .maxMember(chatPost.getMaxMember())
                .title(chatPost.getTitle())
                .status(chatPost.getStatus())
                .build();
            dtoList.add(dto);
        }

        return dtoList;
    }

    /**
     * 글 상세 조회
     *
     * @param id
     * @return
     */
    public ChatPostDetailResponseDto getChatPostDetail(Long id) {
        ChatPost chatPost = chatPostRepository.findById(id)
            .orElseThrow(
                () -> new EntityNotFoundException("글이 조회되지 않습니다.")
            );

        ChatPostDetailResponseDto.ChatPostDetailResponseDtoBuilder builder =
            ChatPostDetailResponseDto.builder()
                .id(chatPost.getId())
                .title(chatPost.getTitle())
                .content(chatPost.getContent())
                .currentMember(String.valueOf(chatPost.getCurrentMember()))
                .maxMember(chatPost.getMaxMember())
                .status(chatPost.getStatus());

        // 🔥 작성자 정보 세팅
        if (chatPost.getHost() != null) {
            builder
                .writeProfileId(chatPost.getHost().getHostId())
                .writerName(chatPost.getHost().getOrganizationName());
        } else if (chatPost.getPerformer() != null) {
            builder
                .writeProfileId(chatPost.getPerformer().getPerformerId())
                .writerName(chatPost.getPerformer().getStageName());
        } else if (chatPost.getProfile() != null) {
            builder
                .writeProfileId(chatPost.getProfile().getProfileId());
            //TODO:프로필에 닉네임 작성있을 시 추가 .writerName(chatPost.getProfile().getProfileId());
        }

        return builder.build();
    }


    /**
     * 글 수정
     *
     * @param chatId
     * @param updateDTO
     */
    public void updateChatPost(Long chatId, ChatPostUpdateRequestDto updateDTO, Long userId) {

        log.info("게시글 수정 시작 - chatId: {}, 수정 요청자ID: {}", chatId, userId);

        try {
            ChatPost chatPost = chatPostRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("글이 존재하지 않습니다. ID: " + chatId));

            checkChatPostAuthority(chatPost, userId); // 권한 체크 로그는 내부 메서드에서 처리

            chatPost.setTitle(updateDTO.getTitle());
            chatPost.setContent(updateDTO.getContent());
            chatPost.setStatus(updateDTO.getStatus());
            log.info("게시글 수정 완료 - chatId: {}", chatId);
        } catch (Exception e) {
            log.error("게시글 수정 실패 - chatId: {}", chatId, e);
            throw e;
        }
    }

    /**
     * post 진입시 표기를 위해 performanceTitle 가져옴
     *
     * @param performanceId
     * @return
     */
    public String getPerformanceTitle(Long performanceId) {
        Performance performance = performanceRepository.findById(performanceId)
            .orElseThrow(
                () -> new EntityNotFoundException("공연이 존재 하지 않습니다.")
            );
        return performance.getTitle();
    }

    /**
     * 권한 체크
     *
     * @param chatPost
     * @param loginProfileId
     */
    public void checkChatPostAuthority(ChatPost chatPost, Long loginProfileId) {
        boolean isHost = chatPost.getHost() != null && chatPost.getHost().getHostId().equals(loginProfileId);
        boolean isPerformer = chatPost.getPerformer() != null && chatPost.getPerformer().getPerformerId().equals(loginProfileId);
        boolean isUser = chatPost.getProfile() != null && chatPost.getProfile().getUser().equals(loginProfileId);

        if (!isHost && !isPerformer && !isUser) {
            log.error("권한 없음 경고 - 요청자 ID: {} 는 해당 게시글의 작성자가 아님", loginProfileId);
            throw new AccessDeniedException("권한이 없습니다.");
        }
        log.info("권한 확인 완료");
    }

}
