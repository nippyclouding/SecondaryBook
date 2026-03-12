package project.chat.message;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MessageMapper {
    List<MessageVO> findByRoomSeq(@Param("chat_room_seq") long chat_room_seq);
    int save(MessageVO message);
    int updateReadStatus(
            @Param("chat_room_seq") long chat_room_seq,
            @Param("member_seq") long member_seq
    );

    boolean isUnreadMessage(@Param("member_seq") long member_seq);

    String findBySellerNicknm(@Param("member_seq") long member_seq);
}
