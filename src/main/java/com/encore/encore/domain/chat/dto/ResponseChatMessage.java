import com.encore.encore.domain.chat.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseChatMessage {
    private Long messageId;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime createdAt;

    public static ResponseChatMessage from(ChatMessage message) {
        return ResponseChatMessage.builder()
            .messageId(message.getMessageId())
            .senderId(message.getSender().getUserId())
            .senderName(message.getSender().getNickname())
            .content(message.getContent())
            .createdAt(message.getCreatedAt())
            .build();
    }
}

