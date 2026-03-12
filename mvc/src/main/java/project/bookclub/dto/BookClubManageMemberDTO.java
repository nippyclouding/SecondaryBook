package project.bookclub.dto;

import lombok.Data;
import project.bookclub.ENUM.JoinStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 독서모임 관리 페이지 - 멤버 목록용 DTO
 * book_club_member + member_info 조인 결과 매핑
 */
@Data
public class BookClubManageMemberDTO {
    // book_club_member 테이블
    private Long bookClubMemberSeq;     // book_club_member_seq
    private Long bookClubSeq;            // book_club_seq
    private Long memberSeq;              // member_seq
    private String leaderYn;             // leader_yn (Boolean → 'Y'/'N' String 매핑)
    private JoinStatus joinSt;           // join_st (JOINED 상태만 조회)
    private LocalDateTime joinStUpdateDtm; // join_st_update_dtm (가입일)

    // member_info 테이블 조인
    private String nickname;             // member_nicknm
    private String profileImgUrl;        // profile_img_url (nullable)

    /**
     * JSP용: LocalDateTime → 포맷팅된 String 반환
     * JSTL fmt:formatDate는 LocalDateTime을 지원하지 않으므로 String으로 변환
     */
    public String getJoinStUpdateDtmText() {
        if (joinStUpdateDtm == null) {
            return "";
        }
        return joinStUpdateDtm.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }
}
