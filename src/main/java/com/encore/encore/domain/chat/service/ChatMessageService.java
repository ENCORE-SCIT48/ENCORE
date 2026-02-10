package com.encore.encore.domain.chat.service;

import com.encore.encore.domain.chat.dto.RequestChatMessage;
import com.encore.encore.domain.chat.dto.ResponseChatMessage;
import com.encore.encore.domain.chat.entity.ChatMessage;
import com.encore.encore.domain.chat.entity.ChatRoom;
import com.encore.encore.domain.chat.repository.ChatMessageRepository;
import com.encore.encore.domain.chat.repository.ChatRoomRepository;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.domain.member.repository.HostProfileRepository;
import com.encore.encore.domain.member.repository.PerformerProfileRepository;
import com.encore.encore.domain.member.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserProfileRepository userProfileRepository;
    private final PerformerProfileRepository performerProfileRepository;
    private final HostProfileRepository hostProfileRepository;

    /**
     * 채팅방에 메시지를 전송합니다.
     *
     * <p>주어진 채팅방(Room)에 현재 프로필(사용자/공연자/주최자)의 메시지를 저장하고,
     * 저장된 메시지를 {@link ResponseChatMessage} DTO로 반환합니다.</p>
     *
     * @param roomId     메시지를 전송할 채팅방의 ID
     * @param activeId   현재 로그인한 사용자의 프로필 ID
     * @param activeMode 현재 로그인한 사용자의 활동 모드(관람객, 공연자, 주최자)
     * @param request    전송할 메시지 내용이 담긴 {@link RequestChatMessage} 객체
     * @return 저장된 메시지를 기반으로 생성된 {@link ResponseChatMessage} 객체
     * @throws IllegalArgumentException 채팅방(roomId)에 해당하는 채팅방이 존재하지 않을 경우
     */
    public ResponseChatMessage sendMessage(Long roomId, Long activeId, ActiveMode activeMode, RequestChatMessage request) {
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));

        ChatMessage message = ChatMessage.builder()
            .room(room)
            .profileId(activeId)
            .profileMode(activeMode)
            .content(request.getContent())
            .build();

        chatMessageRepository.save(message);

        return ResponseChatMessage.builder()
            .messageId(message.getMessageId())
            .profileId(message.getProfileId())
            .senderName(resolveSenderName(message.getMessageId(), message.getProfileMode()))
            .content(message.getContent())
            .createdAt(message.getCreatedAt())
            .build();

    }

    /**
     * 특정 채팅방의 모든 메시지를 조회합니다.
     *
     * <p>조회된 메시지들은 생성일(createdAt) 기준으로 오름차순 정렬되며,
     * {@link ResponseChatMessage} DTO 리스트로 반환됩니다.</p>
     *
     * @param roomId 조회할 채팅방의 ID
     * @return 채팅방에 속한 메시지들을 {@link ResponseChatMessage} 형태로 변환한 리스트
     */
    public List<ResponseChatMessage> getMessages(Long roomId) {

        return chatMessageRepository.findByRoomRoomIdOrderByCreatedAtAsc(roomId)
            .stream()
            .map(message -> ResponseChatMessage.builder()
                .messageId(message.getMessageId())
                .profileId(message.getProfileId())
                .senderName(resolveSenderName(message.getProfileId(), message.getProfileMode()))
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build()
            )
            .toList();
    }


    /**
     * 송신자의 닉네임을 가져오는 메소드
     *
     * @param profileId
     * @param profileMode
     * @return
     */
    public String resolveSenderName(Long profileId, ActiveMode profileMode) {
        return switch (profileMode) {
            case USER -> null;
            case PERFORMER -> performerProfileRepository.findById(profileId)
                .map(p -> p.getStageName()) // 람다 사용
                .orElse("Unknown");
            case HOST -> hostProfileRepository.findById(profileId)
                .map(HostProfile::getOrganizationName)
                .orElse("Unknown");
        };
    }

}
