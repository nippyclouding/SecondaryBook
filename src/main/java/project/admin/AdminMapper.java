package project.admin;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import project.bookclub.vo.BookClubVO;
import project.member.MemberVO;
import project.trade.TradeVO;
import project.util.paging.SearchVO;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdminMapper {
    AdminVO getAdminById(String id);
    // 통계 (카드)
    int countAllMembers();
    int countAllTrades();
    int countAllBookClubs();

    // 검색 (리스트)
    List<MemberVO> searchMembers(SearchVO searchVO);
    int countAllMembersBySearch(SearchVO searchVO);

    List<TradeVO> searchTrades(SearchVO searchVO);
    int countAllTradesBySearch(SearchVO searchVO);

    List<BookClubVO> searchBookClubs(SearchVO searchVO);
    int countAllGroupsBySearch(SearchVO searchVO);

    List<LoginInfoVO> searchAdminLoginLogs(SearchVO searchVO);
    int countAdminLoginLogsBySearch(SearchVO searchVO);

    List<LoginInfoVO> searchUsersLoginLogs(SearchVO searchVO);
    int countUsersLoginLogsBySearch(SearchVO searchVO);

    // 관리 (액션)
    void updateMemberStatus(@Param("seq") Long seq, @Param("status") String status);

    void deleteMember(@Param("seq") Long seq);
    void updateTradeStatus(@Param("seq") Long seq, @Param("status") String status);

    void deleteTrade(@Param("seq") Long seq);

    void deleteBookClub(@Param("seq") Long seq);
    // [NEW] 차트 통계
    List<Map<String, Object>> selectDailySignupStats();
    List<Map<String, Object>> selectDailyTradeStats();

    List<Map<String, Object>> selectCategoryStats();
    // 목록 조회
    List<MemberVO> selectRecentMembers();
    List<TradeVO> selectRecentTrades();

    List<BookClubVO> selectRecentBookClubs();
    // 조회
    List<LoginInfoVO> selectMemberLoginLogs();

    List<LoginInfoVO> selectAdminLoginLogs();
    // 저장/업데이트
    void insertAdminLogin(@Param("admin_seq") Long admin_seq, @Param("login_ip") String login_ip);
    void updateAdminLogout(@Param("admin_seq") Long admin_seq, @Param("logout_ip") String logout_ip);
    void insertMemberLogin(@Param("member_seq") Long member_seq, @Param("login_ip") String login_ip);

    void updateMemberLogout(@Param("member_seq") Long member_seq, @Param("logout_ip") String logout_ip);
    // [추가] 배너 관리 메서드
    List<BannerVO> selectBanners();
    void insertBanner(BannerVO banner);

    void deleteBanner(@Param("bannerSeq") Long seq);
    // [추가] 임시 페이지 관리 메서드
    void insertTempPage(TempPageVO vo);

    TempPageVO selectTempPage(@Param("pageSeq") Long seq);

    // 공지사항 추가
    List<NoticeVO> searchNotices(SearchVO searchVO);
    int countAllNoticesBySearch(SearchVO searchVO);

    void insertNotice(NoticeVO noticeVO);
    void increaseViewCount(@Param("notice_seq") Long notice_seq);

    void deleteNotice(@Param("notice_seq") Long notice_seq);
    void updateNotice(NoticeVO noticeVO);
    List<NoticeVO> selectNotices();

    NoticeVO selectNotice(@Param("notice_seq") Long notice_seq);
    List<NoticeVO> selectActiveNotices(SearchVO searchVO);
    int countActiveNotices(SearchVO searchVO);


    List<TradeVO> searchSafePayList(SearchVO searchVO);

    int countAllSafePayListBySearch(SearchVO searchVO);
}