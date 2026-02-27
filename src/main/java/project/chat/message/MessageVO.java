package project.chat.message;

import lombok.Data;
import java.time.LocalDateTime;


@Data
public class MessageVO {
    private long chat_msg_seq;
    private long chat_room_seq;
    private long trade_seq;

    private long sender_seq;
    private String chat_cont;
    private LocalDateTime sent_dtm;
    private boolean read_yn; // 읽음 여부

    //판매자 / 구매자 닉네임 출력용
    private String member_seller_nicknm;
    private String member_buyer_nicknm;
    /*
    public Date getSentDtmAsDate() {
        return sent_dtm == null ? null : Date.from(sent_dtm.atZone(ZoneId.systemDefault()).toInstant());
    }
     */
}
