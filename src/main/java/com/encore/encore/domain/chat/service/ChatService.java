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
    private final ChatParticipantRepository chatParticipantRepository;
    private final ProfileService profileService;


    /**
     * 채팅 게시글을 저장하고 동시에 채팅방을 생성
     *
     * @param dto 게시글 생성 요청 데이터
     * @return 생성된 게시글 응답 DTO
     * @throws EntityNotFoundException  프로필 정보가 존재하지 않을 경우 발생
     * @throws IllegalArgumentException 작성자 정보가 누락되었을 경우 발생
     */
    public ResponseCreateChatPostDto createChatPostAndRoom(
        RequestCreateChatPostDto dto, Long performanceId, Long activeProfileId, ActiveMode activeMode) {
        log.info("채팅 게시글 생성 프로세스 시작 - 제목: {}", dto.getTitle());

        Performance performance = performanceRepository.findById(performanceId)
            .orElseThrow(() -> new EntityNotFoundException("공연 정보 없음: " + performanceId)
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
                .writerName("임시")
                .writerId(chatPost.getProfileId())
                .writerProfileMode(chatPost.getProfileMode().name())
                .createdAt(chatPost.getCreatedAt())
                .content(chatPost.getContent())
                .currentMember(String.valueOf(chatPost.getCurrentMember()))
                .maxMember(chatPost.getMaxMember())
                .status(chatPost.getStatus());


        return builder.build();
    }


    /**
     * 글 수정
     *
     * @param chatId    수정할 글 id
     * @param updateDTO 수정할 내용이 담긴 dto
     */
    public ResponseUpdateChatPostDto updateChatPost(Long chatId, RequestUpdateChatPostDto updateDTO, Long activeProfileId, ActiveMode activeMode) {
        log.info("게시글 수정 시작 - chatId: {}, 수정 요청자ID: {}", chatId, activeProfileId);

        try {
            ChatPost chatPost = chatPostRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("글이 존재하지 않습니다. ID: " + chatId));

            checkChatPostAuthority(chatPost, activeProfileId, activeMode);

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
     * @param postId     삭제할 글
     * @param activeId   삭제 요청자 id
     * @param activeMode 삭제 요청자 프로필 역할
     * @return
     */
    public ResponseDeleteChatPostDto softDeletePost(Long postId, Long activeId, ActiveMode activeMode) {
        log.info("게시글 논리 삭제 시작 - postId: {}", postId);

        ChatPost chatPost = chatPostRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("글이 조회되지 않습니다. ID: " + postId));

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
     * @param chatPost
     * @param activeId
     * @param activeMode
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
     * 참여중인 채팅방 리스트를 리미트까지 조회
     *
     * @param
     * @param limit
     * @return
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
     * 모든 커뮤니티 채팅방 중 보내진 메시지가 최신인 순인 핫한 채팅방 조회
     *
     * @param limit
     * @return
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
     * chatPost의 id를 가지고 있는 chatRoom을 조회
     *
     * @param id
     * @return
     */
    public Long getChatRoomId(Long id) {
        ChatRoom chatRoom = chatRoomRepository.findByChatPost_Id(id);

        return chatRoom.getRoomId();
    }

    /**
     * 해당 채팅방에 이미 참가자인지 확인하고, 참가하지 않았다면 참가자에 추가한다.
     * 만약 최대 인원수와 참가 인원수가 같거나 상태가 CLOSED일시 참가하지 않은 참가자는 반환한다.
     *
     * @param roomId     채팅방 id
     * @param activeId   로그인 되어있는 유저 프로필 id
     * @param activeMode 선택 되어있는 유저 프로필 역할
     */
    public void getChatAlreadJoin(Long roomId, Long activeId, ActiveMode activeMode) {

        ChatParticipant chatParticipant = chatParticipantRepository
            .findByRoom_RoomIdAndProfileIdAndProfileModeAndIsDeletedFalse(roomId, activeId, activeMode)
            .orElse(null);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new EntityNotFoundException("채팅방이 존재하지 않습니다."));

        ChatPost chatPost = chatPostRepository.findById(chatRoom.getChatPost().getId())
            .orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."));

        if ("CLOSED".equals(chatPost.getStatus().name()) ||
            chatPost.getCurrentMember() >= chatPost.getMaxMember()) {
            throw new IllegalStateException("참여 불가 상태");
        }

        // 1️⃣ 처음 참여
        if (chatParticipant == null) {
            ChatParticipant newParticipant = ChatParticipant.builder()
                .profileId(activeId)
                .profileMode(activeMode)
                .room(chatRoom)
                .participantStatus(ChatParticipant.ParticipantStatus.PENDING)
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
     * roomId의 채팅방에 참여중인 유저 리스트를 가져옴
     *
     * @param roomId
     * @return
     */
    /**
     * 채팅방 참여자 목록을 조회합니다.
     *
     * @param roomId 조회할 채팅방 ID
     * @return 채팅방 참여자 정보 목록 {@link ResponseParticipantDto}
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
                .activeId(chatParticipant.getProfileId())
                .activeMode(chatParticipant.getProfileMode().name())
                .roomId(roomId)
                .build();
            participantDtos.add(dto);
        }

        return participantDtos;
    }

}

