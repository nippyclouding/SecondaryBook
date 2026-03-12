package project.bookclub;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import project.bookclub.ENUM.JoinStatus;
import project.bookclub.ENUM.RequestStatus;
import project.bookclub.vo.BookClubVO;

@Mapper
public interface BookClubMapper {
    // 독서모임 검색 (정렬 + 페이징)
    List<BookClubVO> searchAllWithSort(
            @Param("sort") String sort,
            @Param("limit") int limit,
            @Param("offset") int offset,
            @Param("memberSeq") Long memberSeq);

    List<BookClubVO> searchByKeywordWithSort(
            @Param("tokens") List<String> tokens,
            @Param("sort") String sort,
            @Param("limit") int limit,
            @Param("offset") int offset,
            @Param("memberSeq") Long memberSeq);

    // 전체 개수 조회 (페이징용)
    long countAll();

    long countByKeyword(@Param("tokens") List<String> tokens);

    // 독서모임 상세 조회 (1건)
    BookClubVO selectById(@Param("bookClubSeq") Long bookClubSeq);

    // 독서모임 생성
    void insertBookClub(BookClubVO vo);

    // 독서모임 이름 중복 체크
    int countByName(String book_club_name);

    int countByLeaderAndName(
            @Param("leaderSeq") Long leaderSeq,
            @Param("name") String name);

    // 멤버십 관련
    // 특정 멤버가 JOINED 상태로 가입되어 있는지 확인 (count 반환)
    int selectJoinedMemberCount(@Param("bookClubSeq") Long bookClubSeq,
                                @Param("memberSeq") Long memberSeq);

    // 독서모임의 전체 JOINED 멤버 수 조회
    int getTotalJoinedMemberCount(@Param("bookClubSeq") Long bookClubSeq);

    // 특정 멤버의 대기중인 가입 신청 확인 (request_st='WAIT')
    int selectPendingRequestCount(@Param("bookClubSeq") Long bookClubSeq,
                                  @Param("memberSeq") Long memberSeq);

    // 특정 멤버의 최근 거절된 신청 확인 (request_st='REJECTED')
    int selectRejectedRequestCount(@Param("bookClubSeq") Long bookClubSeq,
                                   @Param("memberSeq") Long memberSeq);

    // 가입 신청 INSERT
    void insertJoinRequest(@Param("bookClubSeq") Long bookClubSeq,
                           @Param("memberSeq") Long memberSeq,
                           @Param("requestCont") String requestCont);

    // 모임장을 book_club_member에 자동 등록 (모임 생성 시 사용)
    void insertLeaderMember(@Param("bookClubSeq") Long bookClubSeq,
                            @Param("memberSeq") Long memberSeq);

    // 찜 관련
    // 독서모임의 찜 개수 조회
    int selectWishCount(@Param("bookClubSeq") Long bookClubSeq);

    // 특정 멤버의 찜 여부 확인
    int selectWishExists(@Param("bookClubSeq") Long bookClubSeq,
                         @Param("memberSeq") Long memberSeq);

    // 찜하기 추가
    int insertWish(@Param("bookClubSeq") Long bookClubSeq,
                   @Param("memberSeq") Long memberSeq);

    // 찜하기 취소
    int deleteWish(@Param("bookClubSeq") Long bookClubSeq,
                   @Param("memberSeq") Long memberSeq);

    // 게시판 관련
    // 독서모임 게시판 - 최근 원글 10개 조회 (member_info 조인)
    List<project.bookclub.vo.BookClubBoardVO> selectRecentRootBoardsByClub(@Param("bookClubSeq") Long bookClubSeq);

    // 독서모임 게시글 단건 조회 (상세 페이지용)
    project.bookclub.vo.BookClubBoardVO selectBoardDetail(@Param("bookClubSeq") Long bookClubSeq,
                                                          @Param("postId") Long postId);

    // 독서모임 게시글의 댓글 목록 조회 (오래된 순)
    List<project.bookclub.vo.BookClubBoardVO> selectBoardComments(@Param("bookClubSeq") Long bookClubSeq,
                                                                  @Param("postId") Long postId);

    // 부모글(원글) 존재 여부 확인 (우회 방지용)
    int existsRootPost(@Param("bookClubSeq") Long bookClubSeq, @Param("postId") Long postId);

    // 댓글 INSERT
    int insertBoardComment(@Param("bookClubSeq") Long bookClubSeq,
                           @Param("postId") Long postId,
                           @Param("memberSeq") Long memberSeq,
                           @Param("commentCont") String commentCont);

    List<BookClubVO> selectMyBookClubs(long member_seq); // 내 모임 조회 추가
    List<BookClubVO> selectWishBookClubs(long member_seq);

    // 관리 페이지 - JOINED 멤버 목록 조회 (member_info 조인)
    List<project.bookclub.dto.BookClubManageMemberDTO> selectJoinedMembersForManage(@Param("bookClubSeq") Long bookClubSeq);

    // 관리 페이지 - WAIT 상태 가입 신청 목록 조회 (member_info 조인)
    List<project.bookclub.dto.BookClubJoinRequestDTO> selectPendingRequestsForManage(@Param("bookClubSeq") Long bookClubSeq);

    // 댓글 UPDATE
    int updateBoardComment(@Param("commentId") Long commentId,
                           @Param("commentCont") String commentCont);

    // 댓글 DELETE (soft delete)
    int deleteBoardComment(@Param("commentId") Long commentId);

    // 댓글 단건 조회 (권한 확인용)
    project.bookclub.vo.BookClubBoardVO selectBoardCommentById(@Param("commentId") Long commentId);

    // 게시글(원글) INSERT
    void insertBoardPost(project.bookclub.vo.BookClubBoardVO boardVO);

    // 관리 페이지 - 가입 신청 승인/거절
    // request 단건 조회 (ID로 조회)
    project.bookclub.dto.BookClubJoinRequestDTO selectRequestById(@Param("requestSeq") Long requestSeq);

    // book_club_member 중복 체크 (book_club_seq, member_seq 조합)
    int selectMemberCount(@Param("bookClubSeq") Long bookClubSeq, @Param("memberSeq") Long memberSeq);

    // join_st 상태만 조회 (재가입 승인 로직용 - 키 문제 방지)
    JoinStatus selectMemberJoinSt(@Param("bookClubSeq") Long bookClubSeq, @Param("memberSeq") Long memberSeq);

    // book_club_member INSERT (승인 시 사용, leader_yn=false)
    void insertMember(@Param("bookClubSeq") Long bookClubSeq, @Param("memberSeq") Long memberSeq);

    // book_club_member UPDATE (재가입 승인 시 LEFT/KICKED/REJECTED -> JOINED로 복구)
    int restoreMemberToJoined(@Param("bookClubSeq") Long bookClubSeq, @Param("memberSeq") Long memberSeq);

    // book_club_request 상태 업데이트 (APPROVED 또는 REJECTED)
    void updateRequestStatus(@Param("requestSeq") Long requestSeq, @Param("status") RequestStatus status);

    // 멤버 강퇴 - 타겟 멤버 상태 조회
    project.bookclub.dto.BookClubManageMemberDTO selectMemberBySeq(@Param("bookClubSeq") Long bookClubSeq, @Param("memberSeq") Long memberSeq);

    // 멤버 강퇴 - join_st='KICKED' 업데이트
    int updateMemberToKicked(@Param("bookClubSeq") Long bookClubSeq, @Param("memberSeq") Long memberSeq);

    // 관리 페이지 - 모임 설정 업데이트 (정보 수정)
    int updateBookClubSettings(@Param("bookClubSeq") Long bookClubSeq,
                               @Param("name") String name,
                               @Param("desc") String desc,
                               @Param("region") String region,
                               @Param("schedule") String schedule,
                               @Param("bannerImgUrl") String bannerImgUrl);

    // 모임명 중복 체크 (현재 모임 제외, 동일 리더의 다른 모임 중복 확인)
    int countByLeaderAndNameExcludingSelf(@Param("leaderSeq") Long leaderSeq,
                                          @Param("name") String name,
                                          @Param("bookClubSeq") Long bookClubSeq);

    // 게시글 수정
    int updateBoardPost(project.bookclub.vo.BookClubBoardVO boardVO);

    // 게시글 삭제 (soft delete)
    int deleteBoardPost(@Param("bookClubSeq") Long bookClubSeq,
                        @Param("postId") Long postId);

    // ===================================
    // 멤버 탈퇴 관련
    // ===================================

    // 멤버 탈퇴 - join_st='LEFT' 업데이트
    int updateMemberToLeft(@Param("bookClubSeq") Long bookClubSeq,
                           @Param("memberSeq") Long memberSeq);

    // 승계 대상 조회 (leader 제외 JOINED 멤버 중 가장 오래된 1명)
    Long selectNextLeaderCandidate(@Param("bookClubSeq") Long bookClubSeq);

    // 새 리더로 승계 (leader_yn=true)
    int updateMemberToLeader(@Param("bookClubSeq") Long bookClubSeq,
                             @Param("newLeaderSeq") Long newLeaderSeq);

    // book_club 리더 변경
    int updateBookClubLeader(@Param("bookClubSeq") Long bookClubSeq,
                             @Param("newLeaderSeq") Long newLeaderSeq);

    // 모임 종료 (soft delete)
    int closeBookClub(@Param("bookClubSeq") Long bookClubSeq);

    // ===================================
    // 멤버 탈퇴 - 내 멤버 상태 조회 (요구사항 SQL #1)
    // ===================================
    /**
     * 내 멤버 상태 조회 (leader_yn, join_st만 조회)
     * - leader_yn: Boolean (DB boolean 그대로)
     * - join_st: String
     */
    java.util.Map<String, Object> selectMyMemberStatus(@Param("bookClubSeq") Long bookClubSeq,
                                                       @Param("memberSeq") Long memberSeq);

    // ===================================
    // 동시성 제어 - book_club 행 잠금 (선택적)
    // ===================================
    /**
     * book_club 행을 FOR UPDATE로 잠금 (비관적 락)
     * 멤버 탈퇴 시 승계/종료 동시성 이슈 방지
     */
    Long lockBookClubForUpdate(@Param("bookClubSeq") Long bookClubSeq);

    // ===================================
    // 게시글/댓글 좋아요 관련
    // ===================================

    // 게시글/댓글의 좋아요 개수 조회
    int selectBoardLikeCount(@Param("boardSeq") Long boardSeq);

    // 특정 멤버의 좋아요 여부 확인
    int selectBoardLikeExists(@Param("boardSeq") Long boardSeq,
                              @Param("memberSeq") Long memberSeq);

    // 좋아요 추가
    int insertBoardLike(@Param("boardSeq") Long boardSeq,
                        @Param("memberSeq") Long memberSeq);

    // 좋아요 취소
    int deleteBoardLike(@Param("boardSeq") Long boardSeq,
                        @Param("memberSeq") Long memberSeq);

    int signOutAll(@Param("member_seq") long member_seq);

    // ===================================
    // Stage 4: 삭제 시 S3 정리용
    // ===================================

    /**
     * 모임의 모든 게시글 이미지 URL 조회 (삭제되지 않은 게시글만)
     * 모임 종료 시 S3 정리용
     * @return board_img_url 리스트 (NULL이 아닌 것만)
     */
    List<String> selectAllBoardImageUrls(@Param("bookClubSeq") Long bookClubSeq);
}
