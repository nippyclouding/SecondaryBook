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
    private Long requestSeq;
    private Long bookClubSeq;
    private Long requestMemberSeq;
    private String requestCont;
    private RequestStatus requestSt;     // WAIT / APPROVED / REJECTED
    private LocalDateTime requestDtm;
    private LocalDate requestProcessedDt; // 승인/거절 처리 날짜

    // member_info 테이블 조인
    private String nickname;
    private String profileImgUrl;

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
