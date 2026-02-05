package com.encore.encore.domain.chat.service;

import com.encore.encore.domain.chat.dto.ChatPostCreateRequestDTO;
import com.encore.encore.domain.chat.entity.ChatPost;
import com.encore.encore.domain.chat.repository.ChatPostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ChatService {
    private final ChatPostRepository ChatPostRepository;


    /**
     * 작성글을 DB에 저장
     *
     * @param dto
     */
    public void createChatPost(ChatPostCreateRequestDTO dto) {
        // Performance 조회 로직은 performance 도메인 구현 후 연결

        ChatPost chatPost = ChatPost.builder()
            //.performance(performance)
            .title(dto.getTitle())
            .content(dto.getContent())
            .maxMember(dto.getMaxMember())
            .build();

        ChatPostRepository.save(chatPost);
    }
}
