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
    AdminVO findByLoginId(String id);
    // --- 통계 ---
    int countAllMembers();
    int countAllTrades();
    int countAllBookClubs();

    // --- 페이징 검색 ---
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

    // --- 관리 액션 ---
    void updateMemberStatus(@Param("seq") Long seq, @Param("status") String status);
    void deleteMember(@Param("seq") Long seq);
    void updateTradeStatus(@Param("seq") Long seq, @Param("status") String status);
    void deleteTrade(@Param("seq") Long seq);
    void deleteBookClub(@Param("seq") Long seq);

    // --- 차트 통계 ---
    List<Map<String, Object>> findDailySignupStats();
    List<Map<String, Object>> findDailyTradeStats();
    List<Map<String, Object>> findCategoryStats();

    // --- 최근 목록 ---
    List<MemberVO> findRecentMembers();
    List<TradeVO> findRecentTrades();
    List<BookClubVO> findRecentBookClubs();

    // --- 로그인 로그 ---
    List<LoginInfoVO> findMemberLoginLogs();
    List<LoginInfoVO> findAdminLoginLogs();
    void saveAdminLoginLog(@Param("admin_seq") Long admin_seq, @Param("login_ip") String login_ip);
    void updateAdminLogout(@Param("admin_seq") Long admin_seq, @Param("logout_ip") String logout_ip);
    void saveMemberLoginLog(@Param("member_seq") Long member_seq, @Param("login_ip") String login_ip);
    void updateMemberLogout(@Param("member_seq") Long member_seq, @Param("logout_ip") String logout_ip);

    // --- 배너 관리 ---
    List<BannerVO> findAllBanners();
    void saveBanner(BannerVO banner);
    void deleteBanner(@Param("bannerSeq") Long seq);

    // --- 임시 페이지 ---
    void saveTempPage(TempPageVO vo);
    TempPageVO findTempPage(@Param("pageSeq") Long seq);

    // --- 공지사항 ---
    List<NoticeVO> searchNotices(SearchVO searchVO);
    int countAllNoticesBySearch(SearchVO searchVO);
    void saveNotice(NoticeVO noticeVO);
    void incrementViewCount(@Param("notice_seq") Long notice_seq);
    void deleteNotice(@Param("notice_seq") Long notice_seq);
    void updateNotice(NoticeVO noticeVO);
    List<NoticeVO> findAllNotices();
    NoticeVO findNotice(@Param("notice_seq") Long notice_seq);
    List<NoticeVO> findAllActiveNotices(SearchVO searchVO);
    int countActiveNotices(SearchVO searchVO);

    // --- 안전결제 ---
    List<TradeVO> searchSafePayList(SearchVO searchVO);
    int countAllSafePayListBySearch(SearchVO searchVO);
}