package project.bookclub.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BookClubBoardVO implements Serializable {
    private Long book_club_seq; // 독서모임의 ID, PK, FK
    private Long book_club_board_seq; // 독서모임 게시판의 게시글 ID, PK
    private Long member_seq; // 멤버 ID, FK
    private Long parent_book_club_board_seq; // 부모 게시글 ID, FK

    private String board_title; // 게시글 제목
    private String board_cont; // 게시글 내용
    private String board_img_url; // 게시글에 첨부된 사진

    private LocalDateTime board_deleted_dtm; // 게시글 삭제 시간
    private LocalDateTime board_crt_dtm; // 게시글 생성 시간
    private LocalDateTime board_upd_dtm; // 게시글 수정 시간

    private String board_crt_dtm_text; // 화면 출력용

    // 조회용 필드 (member_info 조인 시)
    private String member_nicknm; // 작성자 닉네임

    // 댓글 수 (원글 목록 조회 시)
    private Integer comment_count; // 댓글 개수

    private String isbn;
    private String book_title;
    private String book_author;
    private String book_img_url;

    // 좋아요 관련 필드
    private Integer like_count;  // 좋아요 개수
    private Boolean is_liked;    // 현재 로그인 사용자의 좋아요 여부
}
