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
    private Long bookClubMemberSeq;
    private Long bookClubSeq;
    private Long memberSeq;
    private String leaderYn;             // 'Y'/'N' String 매핑
    private JoinStatus joinSt;
    private LocalDateTime joinStUpdateDtm; // 가입일

    // member_info 테이블 조인
    private String nickname;
    private String profileImgUrl;

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
