package project.bookclub.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookClubWishVO {
    private Long book_club_seq; // 독서모임 ID, PK, FK
    private Long book_club_wish_seq; // 독서모임 찜 ID, PK
    private Long member_seq; // 멤버 ID, FK
    private LocalDateTime crt_dtm; // 찜 생성 일시

}
