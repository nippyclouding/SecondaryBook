package project.bookclub.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BookClubRequestVO {
    private Long book_club_seq; // 독서모임 ID, FK
    private Long book_club_request_seq; // 독서모임 가입 요청 ID
    private Long request_member_seq; // 가입을 요청한 멤버 ID

    private String request_cont; // 가입 요청 시 작성한 내용
    private String request_st; // 가입 요청의 상태, ENUM(WAIT / APPROVED / REJECTED)
    private LocalDateTime request_dtm; // 가입 요청 시간
    private LocalDate request_processed_dt; // 가입 요청이 처리된 시간
}