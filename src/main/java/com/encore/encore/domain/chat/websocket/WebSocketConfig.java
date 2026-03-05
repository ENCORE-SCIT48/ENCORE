package com.encore.encore.domain.chat.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * WebSocket 설정 클래스
 * <p>
 * STOMP 프로토콜을 이용한 WebSocket 메시지 통신을 설정합니다.
 * 클라이언트는 /ws 엔드포인트를 통해 연결하며, 서버는 /topic, /queue로 메시지를 브로드캐스트하거나
 * /user/{username} 형태로 특정 사용자에게 전송할 수 있습니다.
 * </p>
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * WebSocket STOMP 엔드포인트 등록
     * <p>
     * 클라이언트가 WebSocket 연결을 시도할 수 있는 경로를 지정합니다.
     * SockJS fallback 지원과 세션 기반 인터셉터를 추가합니다.
     * </p>
     *
     * @param registry STOMP 엔드포인트 레지스트리
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .addInterceptors(new HttpSessionHandshakeInterceptor())
            .withSockJS();
    }

    /**
     * 메시지 브로커 구성
     * <p>
     * 클라이언트의 구독 경로와 메시지 발송 경로를 설정합니다.
     * - enableSimpleBroker: /topic, /queue로 시작하는 경로에 대한 메시지 브로커 활성화
     * - setApplicationDestinationPrefixes: 클라이언트가 메시지를 보낼 때 사용하는 prefix (/app)
     * - setUserDestinationPrefix: 특정 사용자에게 메시지를 보낼 때 사용하는 prefix (/user)
     * </p>
     *
     * @param registry 메시지 브로커 레지스트리
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue"); // 구독용 prefix
        registry.setApplicationDestinationPrefixes("/app"); // 메시지 발송 prefix
        registry.setUserDestinationPrefix("/user");
    }


}
