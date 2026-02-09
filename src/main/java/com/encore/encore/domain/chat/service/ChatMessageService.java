import com.encore.encore.domain.chat.dto.RequestChatMessage;
import com.encore.encore.domain.chat.dto.ResponseChatMessage;
import com.encore.encore.domain.chat.entity.ChatMessage;
import com.encore.encore.domain.chat.entity.ChatRoom;
import com.encore.encore.domain.chat.repository.ChatMessageRepository;
import com.encore.encore.domain.chat.repository.ChatRoomRepository;
import com.encore.encore.domain.user.entity.User;
import com.encore.encore.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;


    public ResponseChatMessage sendMessage(Long roomId, Long userId, RequestChatMessage request) {
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));

        User sender = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        ChatMessage message = ChatMessage.builder()
            .room(room)
            .sender(sender)
            .content(request.getContent())
            .build();

        chatMessageRepository.save(message);

        return ResponseChatMessage.from(message);

    }


    public List<ResponseChatMessage> getMessages(Long roomId) {

        return chatMessageRepository.findByRoomRoomIdOrderByCreatedAtAsc(roomId)
            .stream()
            .map(ResponseChatMessage::from)
            .toList();
    }

}
