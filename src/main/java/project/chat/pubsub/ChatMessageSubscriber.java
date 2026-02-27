package project.chat.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis Pub/Sub 구독자.
 * Redis 채널에서 메시지를 수신하면 SimpMessagingTemplate을 통해
 * 이 서버에 연결된 WebSocket 클라이언트에게 전달한다.
 */
@Slf4j
@Component
public class ChatMessageSubscriber {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public ChatMessageSubscriber(
            SimpMessagingTemplate messagingTemplate,
            @Qualifier("pubsubObjectMapper") ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Redis MessageListenerAdapter에 의해 호출된다.
     * @param message JSON 문자열로 직렬화된 ChatMessage
     */
    public void onMessage(String message) {
        try {
            ChatMessage chatMessage = objectMapper.readValue(message, ChatMessage.class);
            long roomSeq = chatMessage.getChatRoomSeq();

            switch (chatMessage.getType()) {
                case CHAT:
                case PAYMENT:
                case ERROR:
                    messagingTemplate.convertAndSend(
                            "/chatroom/" + roomSeq,
                            chatMessage.getPayload()
                    );
                    break;

                case READ:
                    messagingTemplate.convertAndSend(
                            "/chatroom/" + roomSeq + "/read",
                            chatMessage.getReaderMemberSeq()
                    );
                    break;

                default:
                    log.warn("알 수 없는 메시지 타입: {}", chatMessage.getType());
            }

        } catch (Exception e) {
            log.error("Redis 메시지 처리 실패: {}", message, e);
        }
    }
}
