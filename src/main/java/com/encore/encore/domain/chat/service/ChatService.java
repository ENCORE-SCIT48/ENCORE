package com.encore.encore.domain.chat.service;

import com.encore.encore.domain.chat.dto.*;
import com.encore.encore.domain.chat.entity.ChatParticipant;
import com.encore.encore.domain.chat.entity.ChatPost;
import com.encore.encore.domain.chat.entity.ChatPostType;
import com.encore.encore.domain.chat.entity.ChatRoom;
import com.encore.encore.domain.chat.repository.ChatParticipantRepository;
import com.encore.encore.domain.chat.repository.ChatPostRepository;
import com.encore.encore.domain.chat.repository.ChatRoomRepository;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.member.service.ProfileService;
import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.performance.repository.PerformanceRepository;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 공연 채팅 게시글·채팅방·참가자 비즈니스 로직을 담당하는 서비스.
 * <p>
 * 공연 채팅은 "공연을 본 뒤 이어지는 공간"(후기·택시·뒤풀이 등) 기획에 따라,
 * 공연별로 개별 모집방(CHAT)과 공연 전체 톡방(PERFORMANCE_ALL)을 관리합니다.
 * </p>
 *
 * @see ChatPostType 채팅 유형 (REVIEW, TAXI_SHARE, AFTER_PARTY, GENERAL)
 * @see ChatRoom.RoomType CHAT / DM / PERFORMANCE_ALL
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final ChatPostRepository chatPostRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final PerformanceRepository performanceRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ProfileService profileService;


    /**
     * 공연 소속 채팅 게시글(모집글) 및 해당 채팅방을 생성합니다.
     * <p>
     * 기획: 공연 채팅은 "공연을 본 뒤 이어지는 공간"으로, 후기·감상(REVIEW), 택시 동승(TAXI_SHARE),
     * 뒤풀이(AFTER_PARTY), 일반(GENERAL) 등 목적별로 구분합니다. DTO의 postType(문자열)을
     * 파싱하여 저장하며, null/빈값/잘못된 값은 GENERAL로 처리합니다.
     * </p>
     * <p>
     * 처리 순서: 공연 존재 여부 확인 → ChatPost 저장(postType 포함) → CHAT 타입 ChatRoom 생성
     * → 작성자를 채팅 참가자(PENDING)로 등록 → 응답 DTO 반환.
     * </p>
     *
     * @param dto             생성 요청 (제목, 내용, 모집인원, postType 등)
     * @param performanceId   게시글이 속할 공연 ID (path variable과 일치해야 함)
     * @param activeProfileId 작성자 프로필 ID (현재 로그인 프로필)
     * @param activeMode      작성자 활동 모드 (USER/PERFORMER/HOST)
     * @return 생성된 게시글·채팅방 정보 (postId, chatRoomId, postType 표시명 포함)
     * @throws ApiException 공연이 없으면 NOT_FOUND
     */
    public ResponseCreateChatPostDto createChatPostAndRoom(
        RequestCreateChatPostDto dto, Long performanceId, Long activeProfileId, ActiveMode activeMode) {
        log.info("채팅 게시글 생성 시작 - performanceId={}, title={}, postType={}",
            performanceId, dto.getTitle(), dto.getPostType());

        Performance performance = performanceRepository.findById(performanceId)
            .orElseThrow(() -> {
                log.warn("채팅 생성 실패: 공연 없음 - performanceId={}", performanceId);
                return new ApiException(ErrorCode.NOT_FOUND, "공연 정보 없음: " + performanceId);
            });

        ChatPostType postType = parsePostType(dto.getPostType());
        log.debug("채팅 유형 파싱 결과: {} -> {}", dto.getPostType(), postType);

        ChatPost.ChatPostBuilder builder = ChatPost.builder()
            .performance(performance)
            .title(dto.getTitle())
            .content(dto.getContent())
            .maxMember(dto.getMaxMember())
            .profileId(activeProfileId)
            .profileMode(activeMode)
            .postType(postType)
            .currentMember(0)
            .status(ChatPost.Status.OPEN);

        ChatPost chatPost = builder.build();
        chatPost.addParticipant();
        chatPostRepository.save(chatPost);
        log.info("채팅 게시글 저장 완료 - postId={}, postType={}", chatPost.getId(), postType);

        ChatRoom chatRoom = ChatRoom.builder()
            .chatPost(chatPost)
            .roomType(ChatRoom.RoomType.CHAT)
            .build();
        chatRoomRepository.save(chatRoom);
        log.info("채팅방 생성 완료 - roomId={}, postId={}", chatRoom.getRoomId(), chatPost.getId());

        ChatParticipant chatParticipant = ChatParticipant.builder()
            .profileMode(activeMode)
            .profileId(activeProfileId)
            .room(chatRoom)
            .participantStatus(ChatParticipant.ParticipantStatus.PENDING)
            .build();
        chatParticipantRepository.save(chatParticipant);
        log.debug("채팅 작성자 참가자 등록 완료 - profileId={}", activeProfileId);

        return ResponseCreateChatPostDto.from(chatPost, chatRoom);
    }

    /**
     * 요청 문자열을 {@link ChatPostType}으로 변환. null/빈값/미지원 값은 GENERAL.
     */
    private ChatPostType parsePostType(String postTypeStr) {
        if (postTypeStr == null || postTypeStr.isBlank()) {
            return ChatPostType.GENERAL;
        }
        try {
            return ChatPostType.valueOf(postTypeStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.debug("알 수 없는 postType 무시, GENERAL 사용: {}", postTypeStr);
            return ChatPostType.GENERAL;
        }
    }

    /**
     * 특정 채팅 게시글 상세 조회. 작성자명, 공연명, 채팅 유형(후기/택시/뒤풀이 등) 포함.
     *
     * @param id            조회할 게시글 ID
     * @param performanceId 게시글이 속한 공연 ID (공연명 조회용)
     * @return 상세 DTO (postType, postTypeDisplayName 포함)
     * @throws ApiException 게시글 없으면 NOT_FOUND
     */
    public ResponseDetailChatPostDto getChatPostDetail(Long id, Long performanceId) {
        log.info("채팅 게시글 상세 조회 - postId={}, performanceId={}", id, performanceId);
        ChatPost chatPost = chatPostRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("채팅 게시글 상세 조회 실패: 글 없음 - postId={}", id);
                return new ApiException(ErrorCode.NOT_FOUND, "글이 조회되지 않습니다.");
            });
        String performanceTitle = getPerformanceTitle(performanceId);
        String writerName = profileService.resolveSenderName(chatPost.getProfileId(), chatPost.getProfileMode());
        ChatPostType postType = chatPost.getPostType() != null ? chatPost.getPostType() : ChatPostType.GENERAL;

        ResponseDetailChatPostDto dto = ResponseDetailChatPostDto.builder()
            .id(chatPost.getId())
            .performanceId(performanceId)
            .performanceTitle(performanceTitle)
            .title(chatPost.getTitle())
            .writerName(writerName)
            .writerId(chatPost.getProfileId())
            .writerProfileMode(chatPost.getProfileMode().name())
            .createdAt(chatPost.getCreatedAt())
            .content(chatPost.getContent())
            .currentMember(String.valueOf(chatPost.getCurrentMember()))
            .maxMember(chatPost.getMaxMember())
            .status(chatPost.getStatus().name())
            .postType(postType.name())
            .postTypeDisplayName(postType.getDisplayName())
            .build();
        log.debug("채팅 게시글 상세 조회 완료 - postId={}", id);
        return dto;
    }


    /**
     * [설명] 게시글 수정
     *
     * <p>
     * 게시글 작성자인 경우에만 수정 가능하며, 제목, 내용, 상태를 업데이트합니다.
     * </p>
     *
     * @param chatId          수정할 게시글 ID
     * @param updateDTO       수정 요청 DTO {@link RequestUpdateChatPostDto}
     * @param activeProfileId 요청자 프로필 ID
     * @param activeMode      요청자 활동 모드({@link ActiveMode})
     * @return 수정된 게시글 정보를 담은 {@link ResponseUpdateChatPostDto}
     * @throws ApiException 게시글이 존재하지 않거나 권한이 없는 경우
     */
    public ResponseUpdateChatPostDto updateChatPost(Long chatId, RequestUpdateChatPostDto updateDTO, Long activeProfileId, ActiveMode activeMode) {
        log.info("게시글 수정 시작 - chatId: {}, 수정 요청자ID: {}", chatId, activeProfileId);

        try {
            ChatPost chatPost = chatPostRepository.findById(chatId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "글이 존재하지 않습니다. ID: " + chatId));

            checkChatPostAuthority(chatPost, activeProfileId, activeMode);

            chatPost.setTitle(updateDTO.getTitle());
            chatPost.setContent(updateDTO.getContent());
            chatPost.setStatusFromString(updateDTO.getStatus());

            log.info("게시글 수정 완료 - chatId: {}", chatId);

            return ResponseUpdateChatPostDto.from(chatPost);

        } catch (Exception e) {
            log.error("게시글 수정 실패 - chatId: {}", chatId, e);
            throw e;
        }
    }

    /**
     * [설명] 게시글과 해당 채팅방 논리 삭제
     *
     * <p>
     * 게시글 작성자인 경우 삭제 가능하며, Soft Delete 방식으로 게시글과 채팅방을 삭제 처리합니다.
     * </p>
     *
     * @param postId     삭제할 게시글 ID
     * @param activeId   요청자 프로필 ID
     * @param activeMode 요청자 활동 모드({@link ActiveMode})
     * @return 삭제 처리 결과를 담은 {@link ResponseDeleteChatPostDto}
     * @throws ApiException 게시글이 존재하지 않거나 권한이 없는 경우
     */
    public ResponseDeleteChatPostDto softDeletePost(Long postId, Long activeId, ActiveMode activeMode) {
        log.info("게시글 논리 삭제 시작 - postId: {}", postId);

        ChatPost chatPost = chatPostRepository.findById(postId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "글이 조회되지 않습니다. ID: " + postId));

        log.info("권한 확인");
        checkChatPostAuthority(chatPost, activeId, activeMode);

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
     * 공연 ID로 공연 제목 조회. 채팅 상세/목록에서 공연명 표시 시 사용.
     *
     * @param performanceId 공연 ID
     * @return 공연 타이틀
     * @throws ApiException 공연 없으면 NOT_FOUND
     */
    public String getPerformanceTitle(Long performanceId) {
        Performance performance = performanceRepository.findById(performanceId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연이 존재 하지 않습니다."));
        return performance.getTitle();
    }

    /**
     * [설명] 게시글 권한 체크
     *
     * <p>
     * 로그인 프로필과 게시글 작성자가 일치하는지 확인합니다.
     * </p>
     *
     * @param chatPost   대상 게시글
     * @param activeId   요청자 프로필 ID
     * @param activeMode 요청자 활동 모드({@link ActiveMode})
     * @throws AccessDeniedException 권한이 없을 경우 발생
     */
    public void checkChatPostAuthority(ChatPost chatPost, Long activeId, ActiveMode activeMode) {
        Long postAuthorId = chatPost.getProfileId();
        ActiveMode postAuthorMode = chatPost.getProfileMode();
        if (!activeId.equals(postAuthorId) || !activeMode.equals(postAuthorMode)) {
            log.warn("채팅 게시글 권한 없음 - 요청자({}/{}), 작성자({}/{})",
                activeId, activeMode, postAuthorId, postAuthorMode);
            throw new AccessDeniedException("글 작성자가 아닙니다.");
        }
        log.debug("채팅 게시글 권한 확인 통과 - postId={}", chatPost.getId());
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
        log.info("공연별 채팅방 목록 조회 - performanceId={}, searchType={}, keyword={}, onlyOpen={}",
            performanceId, searchType, keyword, onlyOpen);

        try {
            if (keyword != null && keyword.isBlank()) {
                keyword = null;
            }
            Slice<ResponseListChatPostDto> slice = chatPostRepository.findChatPostList(performanceId, keyword, searchType, onlyOpen, pageable);
            log.debug("채팅방 목록 조회 완료 - performanceId={}, size={}", performanceId, slice.getContent().size());
            return slice;
        } catch (Exception e) {
            log.error("채팅방 목록 조회 실패 - performanceId={}, error={}", performanceId, e.getMessage(), e);
            throw new ApiException(ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * 특정 공연의 "전체 톡방"(PERFORMANCE_ALL)에 해당하는 ChatPost를 조회.
     * 공연당 하나만 존재할 수 있으며, 없으면 null.
     *
     * @param performanceId 공연 ID
     * @return PERFORMANCE_ALL 타입 채팅방의 ChatPost, 없으면 null
     */
    public ChatPost findPerformanceAllChatPost(Long performanceId) {
        log.debug("공연 전체 톡방 조회 - performanceId={}", performanceId);
        return chatPostRepository.findPerformanceAllPost(performanceId)
            .orElse(null);
    }

    /**
     * [설명] 참여 중인 채팅 게시글 리스트 조회 (리미트 적용)
     *
     * @param activeId   로그인 프로필 ID
     * @param activeMode 로그인 프로필 활동 모드({@link ActiveMode})
     * @param limit      조회할 최대 개수
     * @return 참여 중인 게시글 리스트 {@link ResponseMyChatPostDto}
     */
    public List<ResponseMyChatPostDto> getChatPostJoinList(Long activeId, ActiveMode activeMode, int limit) {
        log.info("참여 채팅방 목록 조회(limit) - profileId={}, mode={}, limit={}", activeId, activeMode, limit);
        List<ChatPost> chatPostList = chatPostRepository.findTopParticipatingByProfileAndModeNative(activeId, activeMode.name(), limit);
        List<ResponseMyChatPostDto> result = new ArrayList<>();
        for (ChatPost chatPost : chatPostList) {
            result.add(ResponseMyChatPostDto.from(chatPost));
        }
        log.debug("참여 채팅방 목록 반환 - size={}", result.size());
        return result;
    }

    /**
     * [설명] 모든 커뮤니티 채팅방 중 메시지 최신순으로 조회 (Hot 채팅)
     *
     * @param limit 조회할 최대 개수
     * @return 채팅 게시글 리스트 {@link ResponseMyChatPostDto}
     */
    public List<ResponseMyChatPostDto> getChatListHot(int limit) {

        List<ChatPost> chatPostList = chatPostRepository.findHotChatPosts(limit);

        return chatPostList.stream()
            .map(ResponseMyChatPostDto::from)
            .collect(Collectors.toList());
    }

    /**
     * 로그인한 사용자가 참여 중인 채팅 게시글 목록을 가져옵니다.
     * <p>
     * - 참여자는 `ChatParticipant` 기준으로 필터링됩니다.
     * - 게시글 작성자 필터링을 통해 본인 글만 가져올 수도 있습니다.
     * - 제목/내용 검색이 가능하며, 페이징 처리(Slice)됩니다.
     * - `profileId`와 `profileMode`는 DB에서 enum 이름(String)으로 비교됩니다.
     *
     * @param activeId   현재 로그인한 프로필 ID
     * @param activeMode 현재 로그인한 프로필의 역할(ActiveMode)
     * @param page       조회할 페이지 번호 (0부터 시작)
     * @param size       페이지 당 가져올 게시글 수
     * @param keyword    검색 키워드 (제목 또는 내용)
     * @param searchType 검색 타입 ("title" 또는 "content")
     * @param onlyMine   true이면 본인이 작성한 게시글만 조회
     * @return Slice<ResponseParticipantChatPostDto> 페이징된 참여 게시글 목록
     */
    public Slice<ResponseMyChatPostDto> getChatPostJoinListFull(
        Long activeId,
        ActiveMode activeMode,
        int page,
        int size,
        String keyword,
        String searchType,
        boolean onlyMine
    ) {
        int offset = page * size;

        List<ChatPost> chatPosts = chatPostRepository.findJoinedChats(
            activeId, activeMode.name(), keyword, searchType, onlyMine, size + 1, // 한 페이지+1 개 가져와서 hasNext 판단
            offset
        );

        boolean hasNext = chatPosts.size() > size;

        List<ResponseMyChatPostDto> dtoList = chatPosts.stream()
            .limit(size)
            .map(ResponseMyChatPostDto::from)
            .toList();

        Pageable pageable = PageRequest.of(page, size);
        return new SliceImpl<>(dtoList, pageable, hasNext);
    }

    /**
     * [설명] 게시글 ID로 채팅방 ID 조회
     *
     * @param id 게시글 ID
     * @return 채팅방 ID
     */
    public Long getChatRoomId(Long id) {
        ChatRoom chatRoom = chatRoomRepository.findByChatPost_Id(id);

        return chatRoom.getRoomId();
    }

    /**
     * [설명] 채팅방 참여 여부 확인 및 신규 참가자 추가
     *
     * <p>
     * 최대 인원수 초과 또는 CLOSED 상태인 경우 참여 불가 처리.
     * 이미 참여자면 재입장 시 Soft Delete 복구.
     * </p>
     *
     * @param roomId     채팅방 ID
     * @param activeId   로그인 프로필 ID
     * @param activeMode 로그인 프로필 활동 모드({@link ActiveMode})
     * @throws IllegalStateException 참여 불가 상태일 경우
     */
    public void getChatAlreadJoin(Long roomId, Long activeId, ActiveMode activeMode) {

        // isDeleted 여부 무관하게 조회 → 재입장(소프트삭제 복구) 분기 처리 가능
        ChatParticipant chatParticipant = chatParticipantRepository
            .findByRoom_RoomIdAndProfileIdAndProfileMode(roomId, activeId, activeMode)
            .orElse(null);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "채팅방이 존재하지 않습니다."));

        ChatPost chatPost = chatPostRepository.findById(chatRoom.getChatPost().getId())
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "게시글이 존재하지 않습니다."));

        if (chatParticipant == null) {
            if ("CLOSED".equals(chatPost.getStatus().name()) ||
                chatPost.getCurrentMember() >= chatPost.getMaxMember()) {

                throw new ApiException(ErrorCode.INVALID_REQUEST, "정원이 초과되었거나 마감된 채팅방입니다.");
            }
        }

        // 1️⃣ 처음 참여
        if (chatParticipant == null) {
            ChatParticipant newParticipant = ChatParticipant.builder()
                .profileId(activeId)
                .profileMode(activeMode)
                .room(chatRoom)
                .participantStatus(ChatParticipant.ParticipantStatus.ACCEPTED)
                .build();

            chatParticipantRepository.save(newParticipant);
            chatPost.setCurrentMember(chatPost.getCurrentMember() + 1);
            return;
        }

        // 2️⃣ 재입장 (소프트 삭제 복구)
        if (chatParticipant.isDeleted()) {
            chatParticipant.restore(); // isDeleted=false + 상태 초기화
            chatPost.setCurrentMember(chatPost.getCurrentMember() + 1);
            return;
        }
    }

    /**
     * [설명] 채팅방 참여자 목록 조회
     *
     * @param roomId 조회할 채팅방 ID
     * @return 채팅방 참여자 목록 {@link ResponseParticipantDto}
     */
    public List<ResponseParticipantDto> getChatParticipants(Long roomId) {
        // 채팅방 참여자 엔티티 조회
        List<ChatParticipant> chatParticipantList = chatParticipantRepository.findByRoomRoomId(roomId);

        // DTO 변환
        List<ResponseParticipantDto> participantDtos = new ArrayList<>();
        for (ChatParticipant chatParticipant : chatParticipantList) {
            ResponseParticipantDto dto = ResponseParticipantDto.builder()
                .participantId(chatParticipant.getParticipantId())
                .nickName(profileService.resolveSenderName(
                    chatParticipant.getProfileId(),
                    chatParticipant.getProfileMode()
                ))
                .profileId(chatParticipant.getProfileId())
                .profileMode(chatParticipant.getProfileMode().name())
                .roomId(roomId)
                .build();
            participantDtos.add(dto);
        }

        return participantDtos;
    }

    /**
     * 로그인 한 사용자가 게시물을 수정할 수 있는지 확인한다.
     *
     * @param activeProfileId 로그인 되어있는 프로필 아이디
     * @param activeMode      사용중인 프로필 모드
     * @param dto             글에 대한 정보
     * @return
     */
    public boolean canEdit(Long activeProfileId, ActiveMode activeMode, ResponseDetailChatPostDto dto) {
        return Objects.equals(activeProfileId, dto.getWriterId()) &&
            Objects.equals(activeMode, ActiveMode.valueOf(dto.getWriterProfileMode()));
    }

    public boolean canJoin(Long activeProfileId, ActiveMode activeMode, List<ResponseParticipantDto> chatParticipantList) {

        for (ResponseParticipantDto participant : chatParticipantList) {
            if (participant.getProfileId().equals(activeProfileId)
                && participant.getProfileMode() == activeMode.name()) {
                // 이미 참여자이므로 버튼 보여줌
                return true;
            }
        }
        return false;
    }
}

