package project.chat.chatroom;

import lombok.Data;
import lombok.NoArgsConstructor;
import project.trade.ENUM.SaleStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Data
@NoArgsConstructor
public class ChatroomVO implements Serializable {
    private long chat_room_seq;
    private long trade_seq;
    private LocalDateTime last_msg_dtm;
    private LocalDateTime crt_dtm;
    private String last_msg;
    private String sale_title;
    private long member_buyer_seq;
    private long member_seller_seq;
    private SaleStatus sale_st; // 각 채팅방들의 거래 판매 상태

    //판매자 / 구매자 닉네임 출력용
    private String member_seller_nicknm;
    private String member_buyer_nicknm;

    // 채팅방 안읽음 표시를 위함
    private boolean msg_unread;

    public ChatroomVO(long trade_seq, long member_buyer_seq, long member_seller_seq) {
        this.trade_seq = trade_seq;
        this.member_buyer_seq = member_buyer_seq;
        this.member_seller_seq = member_seller_seq;
    }

    public Date getLastMsgDtmAsDate() {
        return last_msg_dtm == null ? null : Date.from(last_msg_dtm.atZone(ZoneId.systemDefault()).toInstant());
    }
}
