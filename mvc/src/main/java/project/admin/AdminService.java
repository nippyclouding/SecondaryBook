package project.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.bookclub.vo.BookClubVO;
import project.member.MemberVO;
import project.trade.TradeVO;
import project.util.paging.SearchVO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminService {

    private final AdminMapper adminMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminVO login(String id, String rawPwd) {
        AdminVO admin = adminMapper.findByLoginId(id);

        if (admin != null) {
            boolean isMatch = passwordEncoder.matches(rawPwd, admin.getAdmin_password());
            if (isMatch) return admin;
        } else {
            log.warn("해당 아이디의 관리자가 없습니다 : {}", id);
        }
        return null;
    }

    // --- 통계 ---
    public int countAllMembers() { return adminMapper.countAllMembers(); }
    public int countAllTrades() { return adminMapper.countAllTrades(); }
    public int countAllBookClubs() { return adminMapper.countAllBookClubs(); }

    // --- 검색 (API) ---
    public List<MemberVO> searchMembers(SearchVO searchVO) {
        return adminMapper.searchMembers(searchVO);
    }

    public List<TradeVO> searchTrades(SearchVO searchVO) {
        return adminMapper.searchTrades(searchVO);
    }

    public List<TradeVO> searchSafePayList(SearchVO searchVO) {
        return adminMapper.searchSafePayList(searchVO);
    }

    public List<BookClubVO> searchBookClubs(SearchVO searchVO) {
        return adminMapper.searchBookClubs(searchVO);
    }

    public List<LoginInfoVO> searchAdminLoginLogs(SearchVO searchVO) {
        return adminMapper.searchAdminLoginLogs(searchVO);
    }

    public List<LoginInfoVO> searchUsersLoginLogs(SearchVO searchVO) {
        return adminMapper.searchUsersLoginLogs(searchVO);
    }

    @Transactional
    public void handleMemberAction(Long seq, String action) {
        if ("BAN".equals(action)) {
            adminMapper.updateMemberStatus(seq, "BAN"); // 정지 상태
        } else if ("ACTIVE".equals(action)) {
            adminMapper.updateMemberStatus(seq, "JOIN"); // 정상 상태 (JOIN으로 매핑)
        } else if ("DELETE".equals(action)) {
            adminMapper.deleteMember(seq); // 탈퇴 처리 (Deleted DTM 업데이트)
        }
    }

    @Transactional
    public void handleTradeAction(Long seq, String action) {
        if ("DELETE".equals(action)) {
            adminMapper.deleteTrade(seq); // 삭제 처리 (Del DTM 업데이트)
        } else if ("BAN".equals(action)) {
            adminMapper.updateTradeStatus(seq, "BAN"); // 판매 금지 상태
        } else if ("SALE".equals(action)) {
            adminMapper.updateTradeStatus(seq, "SALE"); // 판매 중 상태로 복구 (Undo)
        } else if ("RESERVED".equals(action) || "SOLD".equals(action)) {
            adminMapper.updateTradeStatus(seq, action);
        }
        // 그 외 알 수 없는 action은 무시
    }

    @Transactional
    public void deleteBookClub(Long seq) {
        adminMapper.deleteBookClub(seq);
    }

    // --- 차트 데이터 조회 ---
    public Map<String, Object> getChartData() {
        Map<String, Object> data = new HashMap<>();
        data.put("dailySignups", adminMapper.findDailySignupStats());
        data.put("dailyTrades", adminMapper.findDailyTradeStats());
        data.put("categories", adminMapper.findCategoryStats());
        return data;
    }

    // --- 최근 목록 ---
    public List<MemberVO> getRecentMembers() { return adminMapper.findRecentMembers(); }
    public List<TradeVO> getRecentTrades() { return adminMapper.findRecentTrades(); }
    public List<BookClubVO> getRecentBookClubs() { return adminMapper.findRecentBookClubs(); }

    // --- 로그인 로그 ---
    public List<LoginInfoVO> getMemberLoginLogs() {
        return adminMapper.findMemberLoginLogs();
    }
    public List<LoginInfoVO> getAdminLoginLogs() {
        return adminMapper.findAdminLoginLogs();
    }

    // 관리자 로그인 기록
    public void recordAdminLogin(Long admin_seq, String login_ip) {
        adminMapper.saveAdminLoginLog(admin_seq, login_ip);
    }
    // 관리자 로그아웃 기록
    public void recordAdminLogout(Long admin_seq, String logout_ip) {
        adminMapper.updateAdminLogout(admin_seq, logout_ip);
    }

    // 회원 로그인 기록
    public void recordMemberLogin(Long member_seq, String login_ip) {
        adminMapper.saveMemberLoginLog(member_seq, login_ip);
    }
    // 회원 로그아웃 기록
    public void recordMemberLogout(Long member_seq, String logout_ip) {
        adminMapper.updateMemberLogout(member_seq, logout_ip);
    }

    // --- 배너 관리 ---
    public List<BannerVO> getBanners() {
        return adminMapper.findAllBanners();
    }

    @Transactional
    public void saveBanner(BannerVO banner) {
        adminMapper.saveBanner(banner);
    }

    @Transactional
    public void deleteBanner(Long seq) {
        adminMapper.deleteBanner(seq);
    }

    @Transactional
    public Long saveTempPage(String title, String content) {
        TempPageVO vo = new TempPageVO();
        vo.setTitle(title);
        vo.setContent(content);
        adminMapper.saveTempPage(vo);
        return vo.getPageSeq();
    }

    // 임시 페이지 조회
    public TempPageVO getTempPage(Long id) {
        return adminMapper.findTempPage(id);
    }

    // 공지사항 저장
    public void insertNotice(NoticeVO noticeVO) {
        adminMapper.saveNotice(noticeVO);
    }

    // 공지사항 목록 조회

    public List<NoticeVO> searchNotices(SearchVO searchVO) {
        return adminMapper.searchNotices(searchVO);
    }

    public int countAllNoticesBySearch(SearchVO searchVO) {
        return adminMapper.countAllNoticesBySearch(searchVO);
    }

    public List<NoticeVO> selectNotices() {
        return adminMapper.findAllNotices();
    }

    public List<NoticeVO> selectActiveNotices(SearchVO searchVO) {
        return adminMapper.findAllActiveNotices(searchVO);
    }
    public int countActiveNotices(SearchVO searchVO) {
        return adminMapper.countActiveNotices(searchVO);
    }

    public NoticeVO selectNotice(Long notice_seq) {
        return adminMapper.findNotice(notice_seq);
    }

    @Transactional
    public void increaseViewCount(Long notice_seq) {
        adminMapper.incrementViewCount(notice_seq);
    }

    @Transactional
    public void deleteNotice(Long notice_seq) {
        adminMapper.deleteNotice(notice_seq);
    }

    @Transactional
    public void updateNotice(NoticeVO noticeVO) {
        adminMapper.updateNotice(noticeVO);
    }

    public int countAllMembersBySearch(SearchVO searchVO) {
        return adminMapper.countAllMembersBySearch(searchVO);
    }

    public int countAllTradesBySearch(SearchVO searchVO) {
        return adminMapper.countAllTradesBySearch(searchVO);
    }

    public int countAllSafePayListBySearch(SearchVO searchVO) {
        return adminMapper.countAllSafePayListBySearch(searchVO);
    }

    public int countAllGroupsBySearch(SearchVO searchVO) {
        return adminMapper.countAllGroupsBySearch(searchVO);
    }

    public int countAdminLoginLogsBySearch(SearchVO searchVO) {
        return adminMapper.countAdminLoginLogsBySearch(searchVO);
    }

    public int countUsersLoginLogsBySearch(SearchVO searchVO) {
        return adminMapper.countUsersLoginLogsBySearch(searchVO);
    }
}