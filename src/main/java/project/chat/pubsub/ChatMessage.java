package project.chat.pubsub;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Redis Pub/Sub 채널을 통해 전달되는 메시지 DTO.
 * 일반 채팅, 읽음 이벤트, 결제 알림 등 모든 브로드캐스트 메시지를 담는다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage implements Serializable {

    public enum MessageType {
        CHAT,
        READ,
        PAYMENT,
        ERROR
    }

    private MessageType type;
    private long chatRoomSeq;
    private Object payload;
    private Long readerMemberSeq;
}
