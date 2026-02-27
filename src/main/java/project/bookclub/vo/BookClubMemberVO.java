package project.bookclub.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookClubMemberVO {
    private Long book_club_seq;          // 독서모임 ID, PK, FK
    private Long book_club_member_seq;    // 독서모임에 속한 유저 ID, PK (중간테이블 row id)
    private Long member_seq;            // 멤버ID, FK

    private Boolean leader_yn;          // 모임장 여부
    private String join_st;             // 독서모임 가입 상태, ENUM(예: ACTIVE/PENDING/BANNED 등) -> 일단 String
    private LocalDateTime join_st_update_dtm; // 독서모임 가입 상태 수정 시간
}
