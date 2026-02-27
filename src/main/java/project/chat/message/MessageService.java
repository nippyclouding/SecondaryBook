package project.chat.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final MessageMapper messageMapper;

    // 메시지 저장
    public void saveMessage(MessageVO message) {
        messageMapper.save(message);
        log.info("메시지 저장 완료: roomSeq={}, sender={}", message.getChat_room_seq(), message.getSender_seq());
    }

    // 채팅방 전환 시 전환한 채팅방에 대해 전체 메시지 조회
    public List<MessageVO> getAllMessages(long chat_room_seq, long member_seq) {

        // 채팅방에 들어왔을 경우 해당 회원은 모든 메시지 읽음 처리
        messageMapper.updateReadStatus(chat_room_seq, member_seq);

        return messageMapper.findByRoomSeq(chat_room_seq);
    }

    public boolean isUnreadMessage(long member_seq) {
        return messageMapper.isUnreadMessage(member_seq);
    }

    // 세션값 기준 닉네임 조회
    public String findBySellerNicknm (long member_seq) {
        return messageMapper.findBySellerNicknm(member_seq);
    }

    public void markAsRead(long chat_room_seq, long member_seq) {
        messageMapper.updateReadStatus(chat_room_seq, member_seq);
    }
}
