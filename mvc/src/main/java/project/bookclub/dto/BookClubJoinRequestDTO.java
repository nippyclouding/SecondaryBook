package project.bookclub.dto;

import lombok.Data;
import project.bookclub.ENUM.RequestStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 독서모임 관리 페이지 - 가입 신청 목록용 DTO
 * book_club_request + member_info 조인 결과 매핑
 */
@Data
public class BookClubJoinRequestDTO {
    // book_club_request 테이블
    private Long requestSeq;             // book_club_request_seq
    private Long bookClubSeq;            // book_club_seq
    private Long requestMemberSeq;       // request_member_seq
    private String requestCont;          // request_cont
    private RequestStatus requestSt;     // request_st (WAIT/APPROVED/REJECTED)
    private LocalDateTime requestDtm;    // request_dtm
    private LocalDate requestProcessedDt; // request_processed_dt (승인/거절 처리 날짜)

    // member_info 테이블 조인
    private String nickname;             // member_nicknm
    private String profileImgUrl;        // profile_img_url (nullable)

    /**
     * JSP용: LocalDateTime → 포맷팅된 String 반환
     * JSTL fmt:formatDate는 LocalDateTime을 지원하지 않으므로 String으로 변환
     */
    public String getRequestDtmText() {
        if (requestDtm == null) {
            return "";
        }
        return requestDtm.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
    }
}
