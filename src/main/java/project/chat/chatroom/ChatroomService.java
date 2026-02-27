package project.chat.chatroom;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.util.exception.ServerException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatroomService {

    private final ChatroomMapper chatroomMapper;

    public List<ChatroomVO> searchAll(long member_seq) {
        return chatroomMapper.findAllByMemberSeq(member_seq);
        //select * from chatroom where member_seq = #{member_seq}
    }

    @Transactional
    public ChatroomVO findOrCreateRoom(long member_seller_seq, long member_buyer_seq, long trade_seq) {
        ChatroomVO findChatroom = chatroomMapper.findRoom(member_seller_seq, member_buyer_seq, trade_seq);
        // 이미 채팅 전적이 있다면
        if (findChatroom != null) {
            return findChatroom;
        }

        // 신규 채팅일 때
        try {
            ChatroomVO chatroom = new ChatroomVO(trade_seq, member_buyer_seq, member_seller_seq);
            int result = chatroomMapper.save(chatroom);

            if (result > 0) {
                chatroom = chatroomMapper.findRoom(member_seller_seq, member_buyer_seq, trade_seq);
            } else {
                throw new ServerException("fail to save new chatroom");
            }

            return chatroom;
        } catch (DuplicateKeyException e) {
            // 동시에 생성된 경우 : 다시 조회
            return chatroomMapper.findRoom(member_seller_seq, member_buyer_seq, trade_seq);
        }
    }

    // 허용되지 않은 접근자의 채팅방 접근 체크
    public boolean isMemberOfChatroom(long chat_room_seq, long member_seq) {
        return chatroomMapper.isMemberOfChatroom(chat_room_seq, member_seq);
    }

    public Long findChatRoomSeqByTradeAndBuyer(Long trade_seq, Long buyer_seq) {
        return chatroomMapper.findChatRoomSeqByTradeAndBuyer(trade_seq, buyer_seq);
    }

    // 해당 trade의 구매자(채팅방 참여자)인지 확인
    public boolean isBuyerOfTrade(long trade_seq, long member_seq) {
        return chatroomMapper.isBuyerOfTrade(trade_seq, member_seq);
    }

    // 채팅방 seq로 채팅방 조회
    public ChatroomVO findByChatRoomSeq(long chat_room_seq) {
        return chatroomMapper.findByChatRoomSeq(chat_room_seq);
    }



    public List<ChatroomVO> searchAllWithPaging(long memberSeq, int limit, int offset, String sale_st) {
        return chatroomMapper.findAllByMemberSeqWithPaging(memberSeq, limit, offset, sale_st);
    }

    public int countAll(long member_seq, String sale_st) {
        return chatroomMapper.countByMemberSeq(member_seq, sale_st);
    }

    @Transactional
    public void updateLastMessage(Long chat_room_seq, String last_msg) {
        // null 체크
        if (last_msg == null || last_msg.isEmpty()) {
            return;
        }

        // 이미지 메시지일 경우 처리
        if ("[IMAGE]".equals(last_msg) || last_msg.startsWith("[IMAGE]")) {
            last_msg = "사진을 보냈습니다.";
        }

        // 안전결제 메시지일 경우 처리
        if (last_msg.startsWith("[SAFE_PAYMENT")) {
            last_msg = "안전결제 요청";
        }

        // 길이 50 초과 시 자르기
        if (last_msg.length() > 50) {
            last_msg = last_msg.substring(0, 50) + "...";
        }

        chatroomMapper.updateLastMessage(chat_room_seq, last_msg);
    }
}
