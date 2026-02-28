package com.encore.encore.domain.chat.service;

import com.encore.encore.domain.chat.dto.*;
import com.encore.encore.domain.chat.entity.ChatParticipant;
import com.encore.encore.domain.chat.entity.ChatPost;
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
    private final ChatParticipantRepository chatParticipantRepository;
    private final ProfileService profileService;


    /**
     * [설명] 채팅 게시글 생성 및 채팅방 생성
     *
     * <p>
     * 주어진 DTO를 기반으로 채팅 게시글을 저장하고, 해당 게시글에 대응하는 채팅방을 생성합니다.
     * 생성 후 작성자를 채팅방 참가자로 등록합니다.
     * </p>
     *
     * @param dto             게시글 생성 요청 데이터 {@link RequestCreateChatPostDto}
     * @param performanceId   게시글이 속한 공연 ID
     * @param activeProfileId 작성자 프로필 ID
     * @param activeMode      작성자 활동 모드({@link ActiveMode})
     * @return 생성된 게시글 및 채팅방 정보를 담은 {@link ResponseCreateChatPostDto}
     * @throws ApiException             공연 정보가 존재하지 않을 경우 {@link ErrorCode#NOT_FOUND}
     * @throws IllegalArgumentException 작성자 정보가 누락되었을 경우
     */
    public ResponseCreateChatPostDto createChatPostAndRoom(
        RequestCreateChatPostDto dto, Long performanceId, Long activeProfileId, ActiveMode activeMode) {
        log.info("채팅 게시글 생성 프로세스 시작 - 제목: {}", dto.getTitle());

        Performance performance = performanceRepository.findById(performanceId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연 정보 없음: " + performanceId)
            );

        ChatPost.ChatPostBuilder builder = ChatPost.builder()
            .performance(performance)
            .title(dto.getTitle())
            .content(dto.getContent())
            .maxMember(dto.getMaxMember())
            .profileId(activeProfileId)
            .profileMode(activeMode)
            .currentMember(0)
            .status(ChatPost.Status.OPEN);

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

        ChatParticipant chatParticipant = ChatParticipant.builder()
            .profileMode(activeMode)
            .profileId(activeProfileId)
            .room(chatRoom)
            .participantStatus(ChatParticipant.ParticipantStatus.PENDING)
            .build();
        chatParticipantRepository.save(chatParticipant);

        return ResponseCreateChatPostDto.from(chatPost, chatRoom);
    }

    /**
     * [설명] 특정 게시글 상세 조회
     *
     * @param id 조회할 게시글 ID
     * @return 게시글 상세 정보를 담은 {@link ResponseDetailChatPostDto}
     * @throws ApiException 게시글이 존재하지 않을 경우 {@link ErrorCode#NOT_FOUND}
     */
    public ResponseDetailChatPostDto getChatPostDetail(Long id) {
        ChatPost chatPost = chatPostRepository.findById(id)
            .orElseThrow(
                () -> new ApiException(ErrorCode.NOT_FOUND, "글이 조회되지 않습니다.")
            );

        String writerName = profileService.resolveSenderName(chatPost.getProfileId(), chatPost.getProfileMode());

        ResponseDetailChatPostDto.ResponseDetailChatPostDtoBuilder builder =
            ResponseDetailChatPostDto.builder()
                .id(chatPost.getId())
                .title(chatPost.getTitle())
                .writerName(writerName)
                .writerId(chatPost.getProfileId())
                .writerProfileMode(chatPost.getProfileMode().name())
                .createdAt(chatPost.getCreatedAt())
                .content(chatPost.getContent())
                .currentMember(String.valueOf(chatPost.getCurrentMember()))
                .maxMember(chatPost.getMaxMember())
                .status(chatPost.getStatus().name());


        return builder.build();
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
     * [설명] 특정 공연의 타이틀 조회
     *
     * @param performanceId 공연 ID
     * @return 공연 타이틀 문자열
     * @throws ApiException 공연이 존재하지 않을 경우 {@link ErrorCode#NOT_FOUND}
     */
    public String getPerformanceTitle(Long performanceId) {
        Performance performance = performanceRepository.findById(performanceId)
            .orElseThrow(
                () -> new ApiException(ErrorCode.NOT_FOUND, "공연이 존재 하지 않습니다.")
            );
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
            log.warn("권한 없음 - 로그인 프로필({}/{}) vs 작성자({}/{})",
                activeMode, activeMode, postAuthorId, postAuthorMode);
            throw new AccessDeniedException("글 작성자가 아닙니다.");
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
     * [설명] 특정 공연의 전체 채팅 게시글 조회
     *
     * @param performanceId 공연 ID
     * @return 게시글 객체 {@link ChatPost} 또는 존재하지 않을 경우 null
     */
    public ChatPost findPerformanceAllChatPost(Long performanceId) {
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

        List<ChatPost> chatPostList = chatPostRepository.findTopParticipatingByProfileAndModeNative(activeId, activeMode.name(), limit);
        List<ResponseMyChatPostDto> participatingChatPostDtoList = new ArrayList<>();

        for (ChatPost chatPost : chatPostList) {
            ResponseMyChatPostDto dto = ResponseMyChatPostDto.from(chatPost);

            participatingChatPostDtoList.add(dto);
        }

        return participatingChatPostDtoList;

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

        ChatParticipant chatParticipant = chatParticipantRepository
            .findByRoom_RoomIdAndProfileIdAndProfileModeAndIsDeletedFalse(roomId, activeId, activeMode)
            .orElse(null);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "채팅방이 존재하지 않습니다."));

        ChatPost chatPost = chatPostRepository.findById(chatRoom.getChatPost().getId())
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "게시글이 존재하지 않습니다."));

        if ("CLOSED".equals(chatPost.getStatus().name()) ||
            chatPost.getCurrentMember() >= chatPost.getMaxMember()) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "참여 불가 상태");
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

}

