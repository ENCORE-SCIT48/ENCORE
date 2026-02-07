package com.encore.encore.domain.chat.service;

import com.encore.encore.domain.chat.dto.*;
import com.encore.encore.domain.chat.entity.ChatPost;
import com.encore.encore.domain.chat.entity.ChatRoom;
import com.encore.encore.domain.chat.repository.ChatPostRepository;
import com.encore.encore.domain.chat.repository.ChatRoomRepository;
import com.encore.encore.domain.member.entity.UserProfile;
import com.encore.encore.domain.member.repository.UserProfileRepository;
import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.performance.repsitory.PerformanceRepository;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final ChatPostRepository chatPostRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final PerformanceRepository performanceRepository;
    private final UserProfileRepository userProfileRepository;


    /**
     * 채팅 게시글을 저장하고 동시에 채팅방을 생성
     *
     * @param dto 게시글 생성 요청 데이터
     * @return 생성된 게시글 응답 DTO
     * @throws EntityNotFoundException  프로필 정보가 존재하지 않을 경우 발생
     * @throws IllegalArgumentException 작성자 정보가 누락되었을 경우 발생
     */
    public ResponseCreateChatPostDto createChatPostAndRoom(RequestCreateChatPostDto dto, Long performanceId) {
        log.info("채팅 게시글 생성 프로세스 시작 - 제목: {}", dto.getTitle());

        Performance performance = performanceRepository.findById(performanceId)
            .orElseThrow(() -> new EntityNotFoundException("공연 정보 없음: " + performanceId)
            );

        ChatPost.ChatPostBuilder builder = ChatPost.builder()
            .performance(performance)
            .title(dto.getTitle())
            .content(dto.getContent())
            .maxMember(dto.getMaxMember())
            .currentMember(0)
            .status(ChatPost.Status.OPEN);

        assignProfile(builder, dto);

        ChatPost chatPost = builder.build();
        chatPost.addParticipant();
        chatPostRepository.save(chatPost);
        log.info("게시글 저장 완료 - ID: {}", chatPost.getId());

        ChatRoom chatRoom = ChatRoom.builder()
            .chatPost(chatPost)
            .roomType(ChatRoom.RoomType.CHAT)
            .build();
        chatRoomRepository.save(chatRoom);
        log.info("채팅방 생성 완료 - RoomID: {}", chatRoom.getRoomId());

        return ResponseCreateChatPostDto.from(chatPost, chatRoom);
    }

    /**
     * DTO의 ID 값에 따라 적절한 프로필을 빌더에 할당
     */
    private void assignProfile(ChatPost.ChatPostBuilder builder, RequestCreateChatPostDto dto) {
        log.info("테스트를 위한 프로필 하드코딩 시작");
        UserProfile realProfile = userProfileRepository.findById(1L)
            .orElseThrow(() -> new EntityNotFoundException("테스트를 위한 1번 프로필이 DB에 없습니다. data.sql을 확인하세요."));

        builder.profile(realProfile);
        log.info("실제 프로필(ID: 1) 할당 완료: {}", realProfile.getIntroduction());
    }


    /**
     * 공연별 채팅방 목록 조회 출력
     *
     * @param performanceId 채팅방을 불러올 공연 정보
     * @return 채팅방 리스트 전달
     */
    public List<ResponseListChatPostDto> getChatPostsByPerformance(Long performanceId) {
        List<ChatPost> chatPostList = chatPostRepository.findByPerformance_PerformanceId(performanceId);
        List<ResponseListChatPostDto> dtoList = new ArrayList<>();

        for (ChatPost chatPost : chatPostList) {
            ResponseListChatPostDto dto = ResponseListChatPostDto.builder()
                .id(chatPost.getId())
                .currentMember(chatPost.getCurrentMember())
                .maxMember(chatPost.getMaxMember())
                .title(chatPost.getTitle())
                .status(chatPost.getStatus().name())
                .build();
            dtoList.add(dto);
        }

        return dtoList;
    }

    /**
     * 글 상세 조회
     *
     * @param id 상세 조회할 글
     * @return 글 정보
     */
    public ResponseDetailChatPostDto getChatPostDetail(Long id) {
        ChatPost chatPost = chatPostRepository.findById(id)
            .orElseThrow(
                () -> new EntityNotFoundException("글이 조회되지 않습니다.")
            );

        ResponseDetailChatPostDto.ResponseDetailChatPostDtoBuilder builder =
            ResponseDetailChatPostDto.builder()
                .id(chatPost.getId())
                .title(chatPost.getTitle())
                .content(chatPost.getContent())
                .currentMember(String.valueOf(chatPost.getCurrentMember()))
                .maxMember(chatPost.getMaxMember())
                .status(chatPost.getStatus());

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
        }

        return builder.build();
    }


    /**
     * 글 수정
     *
     * @param chatId    수정할 글 id
     * @param updateDTO 수정할 내용이 담긴 dto
     */
    public ResponseUpdateChatPostDto updateChatPost(Long chatId, RequestUpdateChatPostDto updateDTO, Long userId) {
        log.info("게시글 수정 시작 - chatId: {}, 수정 요청자ID: {}", chatId, userId);

        try {
            ChatPost chatPost = chatPostRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("글이 존재하지 않습니다. ID: " + chatId));

            checkChatPostAuthority(chatPost, userId);

            chatPost.setTitle(updateDTO.getTitle());
            chatPost.setContent(updateDTO.getContent());
            chatPost.setStatus(updateDTO.getStatus());

            log.info("게시글 수정 완료 - chatId: {}", chatId);

            return ResponseUpdateChatPostDto.from(chatPost);

        } catch (Exception e) {
            log.error("게시글 수정 실패 - chatId: {}", chatId, e);
            throw e;
        }
    }

    /**
     * 글과 채팅방 논리 삭제
     *
     * @param userId 글 쓴 유저와 일치하는지 확인
     * @param postId 삭제할 글을 조회할 id
     */
    public ResponseDeleteChatPostDto softDeletePost(Long postId, Long userId) {
        log.info("게시글 논리 삭제 시작 - postId: {}", postId);

        ChatPost chatPost = chatPostRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("글이 조회되지 않습니다. ID: " + postId));

        log.info("권한 확인");
        checkChatPostAuthority(chatPost, userId);

        chatPost.delete();

        ChatRoom chatRoom = chatRoomRepository.findByChatPost_Id(postId);
        if (chatRoom != null) {
            chatRoom.delete();
            log.info("채팅방 논리 삭제 완료 - chatRoomId: {}", chatRoom.getRoomId());
        }

        log.info("게시글 논리 삭제 완료 - postId: {}", postId);

        return ResponseDeleteChatPostDto.builder()
            .postId(postId)
            .title(chatPost.getTitle())
            .chatRoomId(chatRoom != null ? chatRoom.getRoomId() : null)
            .chatPostIsDeleted(chatPost.isDeleted())
            .chatRoomIsDeleted(chatRoom != null && chatRoom.isDeleted())
            .build();
    }


    /**
     * post 진입시 표기를 위해 performanceTitle 가져옴
     *
     * @param performanceId 조회할 공연 id
     * @return 공연 타이틀
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
     * @param chatPost       글 id
     * @param loginProfileId 로그인 하고 있는 id
     */
    public void checkChatPostAuthority(ChatPost chatPost, Long loginProfileId) {
        // 테스트용 하드코딩: 요청자가 누구든, 혹은 특정 ID라면 무조건 통과
        if (loginProfileId.equals(1L)) {
            log.info("테스트 모드: ID 1번 사용자 권한 강제 승인");
            return;
        }
        boolean isHost = chatPost.getHost() != null && chatPost.getHost().getHostId().equals(loginProfileId);
        boolean isPerformer = chatPost.getPerformer() != null && chatPost.getPerformer().getPerformerId().equals(loginProfileId);
        boolean isUser = chatPost.getProfile() != null && chatPost.getProfile().getUser().equals(loginProfileId);

        if (!isHost && !isPerformer && !isUser) {
            log.error("권한 없음 경고 - 요청자 ID: {} 는 해당 게시글의 작성자가 아님", loginProfileId);
            throw new AccessDeniedException("권한이 없습니다.");
        }
        log.info("권한 확인 완료");
    }

    /**
     * [설명] 특정 공연에 종속된 채팅방 목록을 페이징하여 조회합니다.
     *
     * @param performanceId 공연 ID
     * @param searchType    검색 타입 (현재 JPQL에서는 통합 검색으로 처리)
     * @param keyword       검색어 (제목 또는 내용에 포함 여부)
     * @param onlyOpen      모집 중인 방만 보기 필터 여부
     * @param pageable      페이징 정보 (Slice 처리를 위함)
     * @return 페이징 처리된 채팅방 목록 응답 DTO 슬라이스
     */
    public Slice<ResponseListChatPostDto> getChatPostList(Long performanceId, String searchType, String keyword, boolean onlyOpen, Pageable pageable) {
        log.info("채팅방 목록 비즈니스 로직 시작 - performanceId: {}, keyword: {}", performanceId, keyword);

        try {
            if (keyword != null && keyword.isBlank()) {
                keyword = null;
            }

            return chatPostRepository.findChatPostList(performanceId, keyword, searchType, onlyOpen, pageable);

        } catch (Exception e) {
            log.error("채팅방 목록 조회 중 데이터베이스 오류 발생 - performanceId: {}, error: {}", performanceId, e.getMessage(), e);
            throw new ApiException(ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * 공연의 전체 채팅방을 조회
     *
     * @param performanceId
     * @return
     */
    public ChatPost findPerformanceAllChatPost(Long performanceId) {
        return chatPostRepository.findPerformanceAllPost(performanceId)
            .orElse(null);
    }

    /**
     * 참여중인 채팅방 리스트 조회
     *
     * @param loginUserId
     * @param limit
     * @return
     */
    public List<ResponseParticipatingChatPostDto> getChatPostJoinList(Long loginUserId, int limit) {

        List<ChatPost> chatPostList = chatPostRepository.findTopParticipatingByUserIdNative(loginUserId, limit);
        List<ResponseParticipatingChatPostDto> participatingChatPostDtoList = new ArrayList<>();

        for (ChatPost chatPost : chatPostList) {
            ResponseParticipatingChatPostDto dto = ResponseParticipatingChatPostDto.from(chatPost);

            participatingChatPostDtoList.add(dto);
        }

        return participatingChatPostDtoList;

    }

    /**
     * 보내진 메시지가 최신인 순의 채팅방 조회
     *
     * @param limit
     * @return
     */
    public List<ResponseParticipatingChatPostDto> getChatListHot(int limit) {

        List<ChatPost> chatPostList = chatPostRepository.findHotChatPosts(limit);

        return chatPostList.stream()
            .map(ResponseParticipatingChatPostDto::from)
            .collect(Collectors.toList());
    }

    /**
     * 참여중인 전체 채팅방을 조회하고 키워드 별 검색함
     *
     * @param userId
     * @param page
     * @param size
     * @param keyword
     * @param searchType
     * @return
     */

    public Slice<ResponseParticipatingChatPostDto> getChatPostJoinListFull(
        Long userId,
        int page,
        int size,
        String keyword,
        String searchType
    ) {
        int offset = page * size;

        List<ChatPost> chatPosts = chatPostRepository.findJoinedChats(
            userId,
            keyword,
            searchType,
            size + 1, // 한 페이지+1 개 가져와서 hasNext 판단
            offset
        );

        boolean hasNext = chatPosts.size() > size;

        List<ResponseParticipatingChatPostDto> dtoList = chatPosts.stream()
            .limit(size)
            .map(ResponseParticipatingChatPostDto::from)
            .toList();

        Pageable pageable = PageRequest.of(page, size);
        return new SliceImpl<>(dtoList, pageable, hasNext);
    }


}

