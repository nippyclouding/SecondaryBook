package project.chat.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

/**
 * Redis Pub/Sub 발행자.
 * 기존 messagingTemplate.convertAndSend() 대신 이 클래스의 메서드를 호출하면
 * Redis를 통해 모든 서버 인스턴스에 메시지가 전달된다.
 */
@Slf4j
@Component
public class ChatMessagePublisher {

    private final StringRedisTemplate redisTemplate;
    private final ChannelTopic chatTopic;
    private final ObjectMapper objectMapper;

    public ChatMessagePublisher(
            StringRedisTemplate redisTemplate,
            ChannelTopic chatTopic,
            @Qualifier("pubsubObjectMapper") ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.chatTopic = chatTopic;
        this.objectMapper = objectMapper;
    }

    public void publishChat(long chatRoomSeq, Object payload) {
        publish(ChatMessage.builder()
                .type(ChatMessage.MessageType.CHAT)
                .chatRoomSeq(chatRoomSeq)
                .payload(payload)
                .build());
    }

    public void publishRead(long chatRoomSeq, long readerMemberSeq) {
        publish(ChatMessage.builder()
                .type(ChatMessage.MessageType.READ)
                .chatRoomSeq(chatRoomSeq)
                .readerMemberSeq(readerMemberSeq)
                .build());
    }

    public void publishPayment(long chatRoomSeq, Object payload) {
        publish(ChatMessage.builder()
                .type(ChatMessage.MessageType.PAYMENT)
                .chatRoomSeq(chatRoomSeq)
                .payload(payload)
                .build());
    }

    public void publishError(long chatRoomSeq, Object payload) {
        publish(ChatMessage.builder()
                .type(ChatMessage.MessageType.ERROR)
                .chatRoomSeq(chatRoomSeq)
                .payload(payload)
                .build());
    }

    private void publish(ChatMessage chatMessage) {
        try {
            String json = objectMapper.writeValueAsString(chatMessage);
            redisTemplate.convertAndSend(chatTopic.getTopic(), json);
        } catch (Exception e) {
            log.error("Redis 메시지 발행 실패: chatRoomSeq={}", chatMessage.getChatRoomSeq(), e);
        }
    }
}
