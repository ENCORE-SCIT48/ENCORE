package com.encore.encore.domain.chat.service;

import com.encore.encore.domain.chat.repository.ChatPostRepository;
import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.performance.repository.PerformanceRepository;
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
    private final PerformanceRepository PerformanceRepository;


    public Performance getPerformanceForChatPost(Long performanceId) {

    }
}
