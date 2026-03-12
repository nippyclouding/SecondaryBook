package project.chat.chatroom;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ChatroomMapper {
    public int save(ChatroomVO chatroomVO);
    public ChatroomVO findRoom(
            @Param("member_seller_seq") long member_seller_seq,
            @Param("member_buyer_seq") long member_buyer_seq,
            @Param("trade_seq") long trade_seq
    );
    public List<ChatroomVO> findAllByMemberSeq(@Param("member_seq") long member_seq);


    // 채팅룸 페이징 처리 전용
    List<ChatroomVO> findAllByMemberSeqWithPaging(
            @Param("member_seq") long member_seq,
            @Param("limit") int limit,
            @Param("offset") int offset,
            @Param("sale_st") String sale_st
    );

    // 전체 개수 조회 (hasMore 판단용)
    int countByMemberSeq(@Param("member_seq") long member_seq,
                         @Param("sale_st") String sale_st);

    public boolean isMemberOfChatroom(@Param("chat_room_seq") long chat_room_seq,
                                      @Param("member_seq") long member_seq);

    Long findChatRoomSeqByTradeAndBuyer(@Param("trade_seq") Long trade_seq,
                                   @Param("buyer_seq") Long buyer_seq
                                    );

    // 해당 trade의 구매자(채팅방 참여자 중 판매자가 아닌 사람)인지 확인
    boolean isBuyerOfTrade(@Param("trade_seq") long trade_seq, @Param("member_seq") long member_seq);

    // 채팅방 seq로 채팅방 조회
    ChatroomVO findByChatRoomSeq(@Param("chat_room_seq") long chat_room_seq);

    void updateLastMessage(@Param("chat_room_seq") long chat_room_seq,
                           @Param("last_msg") String last_msg);

    // public List<MessageVO> findAllByChatRoomSeq(long chat_room_seq); -> message mapper 에서 처리
}
