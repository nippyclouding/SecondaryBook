package project.bookclub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.bookclub.vo.BookClubVO;

/**
 * 독서모임 관리 페이지 전용 ViewModel
 * JSP ${bookclub.*} 매핑용 - 평평한 필드 구조
 *
 * JSP 참조 필드:
 * - ${bookclub.name}
 * - ${bookclub.bookClubSeq}
 * - ${bookclub.memberCount}
 * - ${bookclub.maxMember}
 * - ${bookclub.region}
 * - ${bookclub.schedule}
 * - ${bookclub.description}
 * - ${bookclub.bannerImgUrl}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookClubManageViewDTO {
    private Long bookClubSeq;      // book_club_seq
    private String name;            // book_club_name
    private String description;     // book_club_desc
    private String region;          // book_club_rg
    private Integer maxMember;      // book_club_max_member
    private String schedule;        // book_club_schedule
    private String bannerImgUrl;    // banner_img_url
    private Integer memberCount;    // 현재 참여 인원 (동적 계산)

    /**
     * BookClubVO + 현재 인원 수로부터 ViewModel 생성
     * @param vo BookClubVO (DB 조회 결과)
     * @param currentMemberCount 현재 JOINED 상태 멤버 수
     */
    public BookClubManageViewDTO(BookClubVO vo, int currentMemberCount) {
        this.bookClubSeq = vo.getBook_club_seq();
        this.name = vo.getBook_club_name();
        this.description = vo.getBook_club_desc();
        this.region = vo.getBook_club_rg();
        this.maxMember = vo.getBook_club_max_member();
        this.schedule = vo.getBook_club_schedule();
        this.bannerImgUrl = vo.getBanner_img_url();
        this.memberCount = currentMemberCount;
    }
}
