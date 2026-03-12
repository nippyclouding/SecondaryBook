package project.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import project.bookclub.vo.BookClubVO;
import project.member.MemberVO;
import project.trade.TradeVO;
import project.util.paging.SearchVO;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    AdminMapper adminMapper;

    @Mock
    BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    AdminService adminService;

    // === login ===

    @Nested
    @DisplayName("login - 관리자 로그인")
    class Login {

        @Test
        @DisplayName("아이디와 비밀번호가 일치하면 AdminVO를 반환한다")
        void successfulLogin() {
            // given
            AdminVO admin = new AdminVO();
            admin.setAdmin_seq(1L);
            admin.setAdmin_login_id("admin");
            admin.setAdmin_password("$2a$10$hashedpassword");

            when(adminMapper.getAdminById("admin")).thenReturn(admin);
            when(passwordEncoder.matches("rawPwd", "$2a$10$hashedpassword")).thenReturn(true);

            // when
            AdminVO result = adminService.login("admin", "rawPwd");

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAdmin_login_id()).isEqualTo("admin");
        }

        @Test
        @DisplayName("비밀번호가 틀리면 null을 반환한다")
        void wrongPassword() {
            // given
            AdminVO admin = new AdminVO();
            admin.setAdmin_password("$2a$10$hashedpassword");
            when(adminMapper.getAdminById("admin")).thenReturn(admin);
            when(passwordEncoder.matches("wrong", "$2a$10$hashedpassword")).thenReturn(false);

            // when
            AdminVO result = adminService.login("admin", "wrong");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 아이디면 null을 반환한다")
        void nonExistentId() {
            // given
            when(adminMapper.getAdminById("unknown")).thenReturn(null);

            // when
            AdminVO result = adminService.login("unknown", "anyPwd");

            // then
            assertThat(result).isNull();
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }
    }

    // === 통계 ===

    @Nested
    @DisplayName("통계 메서드")
    class Statistics {

        @Test
        @DisplayName("countAllMembers - 전체 회원 수를 반환한다")
        void countAllMembers() {
            when(adminMapper.countAllMembers()).thenReturn(100);
            assertThat(adminService.countAllMembers()).isEqualTo(100);
        }

        @Test
        @DisplayName("countAllTrades - 전체 거래 수를 반환한다")
        void countAllTrades() {
            when(adminMapper.countAllTrades()).thenReturn(50);
            assertThat(adminService.countAllTrades()).isEqualTo(50);
        }

        @Test
        @DisplayName("countAllBookClubs - 전체 독서모임 수를 반환한다")
        void countAllBookClubs() {
            when(adminMapper.countAllBookClubs()).thenReturn(20);
            assertThat(adminService.countAllBookClubs()).isEqualTo(20);
        }
    }

    // === 검색 ===

    @Nested
    @DisplayName("검색 메서드")
    class Search {

        @Test
        @DisplayName("searchMembers - 회원 검색 결과를 반환한다")
        void searchMembers() {
            SearchVO searchVO = new SearchVO();
            List<MemberVO> members = Arrays.asList(new MemberVO(), new MemberVO());
            when(adminMapper.searchMembers(searchVO)).thenReturn(members);

            assertThat(adminService.searchMembers(searchVO)).hasSize(2);
        }

        @Test
        @DisplayName("searchTrades - 거래 검색 결과를 반환한다")
        void searchTrades() {
            SearchVO searchVO = new SearchVO();
            List<TradeVO> trades = Collections.singletonList(new TradeVO());
            when(adminMapper.searchTrades(searchVO)).thenReturn(trades);

            assertThat(adminService.searchTrades(searchVO)).hasSize(1);
        }

        @Test
        @DisplayName("searchSafePayList - 안전결제 목록을 반환한다")
        void searchSafePayList() {
            SearchVO searchVO = new SearchVO();
            List<TradeVO> list = Arrays.asList(new TradeVO());
            when(adminMapper.searchSafePayList(searchVO)).thenReturn(list);

            assertThat(adminService.searchSafePayList(searchVO)).hasSize(1);
        }

        @Test
        @DisplayName("searchBookClubs - 독서모임 검색 결과를 반환한다")
        void searchBookClubs() {
            SearchVO searchVO = new SearchVO();
            List<BookClubVO> clubs = Arrays.asList(new BookClubVO());
            when(adminMapper.searchBookClubs(searchVO)).thenReturn(clubs);

            assertThat(adminService.searchBookClubs(searchVO)).hasSize(1);
        }

        @Test
        @DisplayName("searchAdminLoginLogs - 관리자 로그인 로그를 반환한다")
        void searchAdminLoginLogs() {
            SearchVO searchVO = new SearchVO();
            List<LoginInfoVO> logs = Arrays.asList(new LoginInfoVO());
            when(adminMapper.searchAdminLoginLogs(searchVO)).thenReturn(logs);

            assertThat(adminService.searchAdminLoginLogs(searchVO)).hasSize(1);
        }

        @Test
        @DisplayName("searchUsersLoginLogs - 회원 로그인 로그를 반환한다")
        void searchUsersLoginLogs() {
            SearchVO searchVO = new SearchVO();
            List<LoginInfoVO> logs = Arrays.asList(new LoginInfoVO(), new LoginInfoVO());
            when(adminMapper.searchUsersLoginLogs(searchVO)).thenReturn(logs);

            assertThat(adminService.searchUsersLoginLogs(searchVO)).hasSize(2);
        }
    }

    // === handleMemberAction ===

    @Nested
    @DisplayName("handleMemberAction - 회원 관리 액션")
    class HandleMemberAction {

        @Test
        @DisplayName("BAN 액션으로 회원을 정지시킨다")
        void banMember() {
            // when
            adminService.handleMemberAction(1L, "BAN");

            // then
            verify(adminMapper).updateMemberStatus(1L, "BAN");
            verify(adminMapper, never()).deleteMember(anyLong());
        }

        @Test
        @DisplayName("ACTIVE 액션으로 회원을 정상 상태(JOIN)로 복구한다")
        void activateMember() {
            // when
            adminService.handleMemberAction(1L, "ACTIVE");

            // then
            verify(adminMapper).updateMemberStatus(1L, "JOIN");
        }

        @Test
        @DisplayName("DELETE 액션으로 회원을 탈퇴 처리한다")
        void deleteMember() {
            // when
            adminService.handleMemberAction(1L, "DELETE");

            // then
            verify(adminMapper).deleteMember(1L);
            verify(adminMapper, never()).updateMemberStatus(anyLong(), anyString());
        }

        @Test
        @DisplayName("알 수 없는 액션은 아무 동작도 하지 않는다")
        void unknownAction() {
            // when
            adminService.handleMemberAction(1L, "UNKNOWN");

            // then
            verify(adminMapper, never()).updateMemberStatus(anyLong(), anyString());
            verify(adminMapper, never()).deleteMember(anyLong());
        }
    }

    // === handleTradeAction ===

    @Nested
    @DisplayName("handleTradeAction - 거래 관리 액션")
    class HandleTradeAction {

        @Test
        @DisplayName("DELETE 액션으로 거래를 삭제 처리한다")
        void deleteTrade() {
            // when
            adminService.handleTradeAction(10L, "DELETE");

            // then
            verify(adminMapper).deleteTrade(10L);
            verify(adminMapper, never()).updateTradeStatus(anyLong(), anyString());
        }

        @Test
        @DisplayName("BAN 액션으로 거래를 판매금지 상태로 변경한다")
        void banTrade() {
            // when
            adminService.handleTradeAction(10L, "BAN");

            // then
            verify(adminMapper).updateTradeStatus(10L, "BAN");
        }

        @Test
        @DisplayName("SALE 액션으로 거래를 판매중 상태로 복구한다")
        void saleTrade() {
            // when
            adminService.handleTradeAction(10L, "SALE");

            // then
            verify(adminMapper).updateTradeStatus(10L, "SALE");
        }

        @Test
        @DisplayName("그 외 액션(RESERVED, SOLD 등)은 해당 상태로 업데이트한다")
        void otherAction() {
            // when
            adminService.handleTradeAction(10L, "RESERVED");

            // then
            verify(adminMapper).updateTradeStatus(10L, "RESERVED");
        }
    }

    // === deleteBookClub ===

    @Test
    @DisplayName("deleteBookClub - 독서모임을 삭제한다")
    void deleteBookClub() {
        adminService.deleteBookClub(5L);
        verify(adminMapper).deleteBookClub(5L);
    }

    // === getChartData ===

    @Test
    @DisplayName("getChartData - 차트 데이터를 반환한다")
    void getChartData() {
        // given
        when(adminMapper.selectDailySignupStats()).thenReturn(Collections.emptyList());
        when(adminMapper.selectDailyTradeStats()).thenReturn(Collections.emptyList());
        when(adminMapper.selectCategoryStats()).thenReturn(Collections.emptyList());

        // when
        Map<String, Object> result = adminService.getChartData();

        // then
        assertThat(result).containsKeys("dailySignups", "dailyTrades", "categories");
    }

    // === 최근 목록 조회 ===

    @Nested
    @DisplayName("최근 목록 조회")
    class RecentLists {

        @Test
        @DisplayName("getRecentMembers - 최근 가입 회원을 반환한다")
        void getRecentMembers() {
            List<MemberVO> members = Arrays.asList(new MemberVO());
            when(adminMapper.selectRecentMembers()).thenReturn(members);
            assertThat(adminService.getRecentMembers()).hasSize(1);
        }

        @Test
        @DisplayName("getRecentTrades - 최근 거래를 반환한다")
        void getRecentTrades() {
            List<TradeVO> trades = Arrays.asList(new TradeVO());
            when(adminMapper.selectRecentTrades()).thenReturn(trades);
            assertThat(adminService.getRecentTrades()).hasSize(1);
        }

        @Test
        @DisplayName("getRecentBookClubs - 최근 독서모임을 반환한다")
        void getRecentBookClubs() {
            List<BookClubVO> clubs = Arrays.asList(new BookClubVO());
            when(adminMapper.selectRecentBookClubs()).thenReturn(clubs);
            assertThat(adminService.getRecentBookClubs()).hasSize(1);
        }
    }

    // === 로그인/로그아웃 기록 ===

    @Nested
    @DisplayName("로그인/로그아웃 기록")
    class LoginLogout {

        @Test
        @DisplayName("관리자 로그인 기록을 저장한다")
        void recordAdminLogin() {
            adminService.recordAdminLogin(1L, "192.168.0.1");
            verify(adminMapper).insertAdminLogin(1L, "192.168.0.1");
        }

        @Test
        @DisplayName("관리자 로그아웃 기록을 업데이트한다")
        void recordAdminLogout() {
            adminService.recordAdminLogout(1L, "192.168.0.1");
            verify(adminMapper).updateAdminLogout(1L, "192.168.0.1");
        }

        @Test
        @DisplayName("회원 로그인 기록을 저장한다")
        void recordMemberLogin() {
            adminService.recordMemberLogin(10L, "10.0.0.1");
            verify(adminMapper).insertMemberLogin(10L, "10.0.0.1");
        }

        @Test
        @DisplayName("회원 로그아웃 기록을 업데이트한다")
        void recordMemberLogout() {
            adminService.recordMemberLogout(10L, "10.0.0.1");
            verify(adminMapper).updateMemberLogout(10L, "10.0.0.1");
        }

        @Test
        @DisplayName("getMemberLoginLogs - 회원 로그인 로그를 반환한다")
        void getMemberLoginLogs() {
            List<LoginInfoVO> logs = Arrays.asList(new LoginInfoVO());
            when(adminMapper.selectMemberLoginLogs()).thenReturn(logs);
            assertThat(adminService.getMemberLoginLogs()).hasSize(1);
        }

        @Test
        @DisplayName("getAdminLoginLogs - 관리자 로그인 로그를 반환한다")
        void getAdminLoginLogs() {
            List<LoginInfoVO> logs = Arrays.asList(new LoginInfoVO());
            when(adminMapper.selectAdminLoginLogs()).thenReturn(logs);
            assertThat(adminService.getAdminLoginLogs()).hasSize(1);
        }
    }

    // === 배너 관리 ===

    @Nested
    @DisplayName("배너 관리")
    class BannerManagement {

        @Test
        @DisplayName("getBanners - 배너 목록을 반환한다")
        void getBanners() {
            List<BannerVO> banners = Arrays.asList(new BannerVO());
            when(adminMapper.selectBanners()).thenReturn(banners);
            assertThat(adminService.getBanners()).hasSize(1);
        }

        @Test
        @DisplayName("saveBanner - 배너를 저장한다")
        void saveBanner() {
            BannerVO banner = new BannerVO();
            banner.setTitle("테스트 배너");
            adminService.saveBanner(banner);
            verify(adminMapper).insertBanner(banner);
        }

        @Test
        @DisplayName("deleteBanner - 배너를 삭제한다")
        void deleteBanner() {
            adminService.deleteBanner(1L);
            verify(adminMapper).deleteBanner(1L);
        }
    }

    // === 임시 페이지 관리 ===

    @Nested
    @DisplayName("임시 페이지 관리")
    class TempPageManagement {

        @Test
        @DisplayName("saveTempPage - 임시 페이지를 저장하고 seq를 반환한다")
        void saveTempPage() {
            // given
            doAnswer(invocation -> {
                TempPageVO vo = invocation.getArgument(0);
                vo.setPageSeq(100L);
                return null;
            }).when(adminMapper).insertTempPage(any(TempPageVO.class));

            // when
            Long result = adminService.saveTempPage("제목", "<p>내용</p>");

            // then
            assertThat(result).isEqualTo(100L);
            verify(adminMapper).insertTempPage(any(TempPageVO.class));
        }

        @Test
        @DisplayName("getTempPage - 임시 페이지를 조회한다")
        void getTempPage() {
            TempPageVO vo = new TempPageVO();
            vo.setPageSeq(100L);
            vo.setTitle("제목");
            when(adminMapper.selectTempPage(100L)).thenReturn(vo);

            TempPageVO result = adminService.getTempPage(100L);
            assertThat(result.getTitle()).isEqualTo("제목");
        }
    }

    // === 공지사항 관리 ===

    @Nested
    @DisplayName("공지사항 관리")
    class NoticeManagement {

        @Test
        @DisplayName("insertNotice - 공지사항을 저장한다")
        void insertNotice() {
            NoticeVO notice = new NoticeVO();
            notice.setNotice_title("공지");
            adminService.insertNotice(notice);
            verify(adminMapper).insertNotice(notice);
        }

        @Test
        @DisplayName("searchNotices - 공지사항을 검색한다")
        void searchNotices() {
            SearchVO searchVO = new SearchVO();
            List<NoticeVO> notices = Arrays.asList(new NoticeVO());
            when(adminMapper.searchNotices(searchVO)).thenReturn(notices);
            assertThat(adminService.searchNotices(searchVO)).hasSize(1);
        }

        @Test
        @DisplayName("selectNotice - 공지사항 단건을 조회한다")
        void selectNotice() {
            NoticeVO notice = new NoticeVO();
            notice.setNotice_seq(1L);
            when(adminMapper.selectNotice(1L)).thenReturn(notice);
            assertThat(adminService.selectNotice(1L).getNotice_seq()).isEqualTo(1L);
        }

        @Test
        @DisplayName("increaseViewCount - 조회수를 증가시킨다")
        void increaseViewCount() {
            adminService.increaseViewCount(1L);
            verify(adminMapper).increaseViewCount(1L);
        }

        @Test
        @DisplayName("deleteNotice - 공지사항을 삭제한다")
        void deleteNotice() {
            adminService.deleteNotice(1L);
            verify(adminMapper).deleteNotice(1L);
        }

        @Test
        @DisplayName("updateNotice - 공지사항을 수정한다")
        void updateNotice() {
            NoticeVO notice = new NoticeVO();
            notice.setNotice_seq(1L);
            notice.setNotice_title("수정된 공지");
            adminService.updateNotice(notice);
            verify(adminMapper).updateNotice(notice);
        }

        @Test
        @DisplayName("selectActiveNotices - 활성 공지사항 목록을 반환한다")
        void selectActiveNotices() {
            SearchVO searchVO = new SearchVO();
            List<NoticeVO> notices = Arrays.asList(new NoticeVO());
            when(adminMapper.selectActiveNotices(searchVO)).thenReturn(notices);
            assertThat(adminService.selectActiveNotices(searchVO)).hasSize(1);
        }
    }

    // === count 메서드 ===

    @Nested
    @DisplayName("검색 결과 카운트 메서드")
    class CountBySearch {

        @Test
        @DisplayName("countAllMembersBySearch")
        void countAllMembersBySearch() {
            SearchVO searchVO = new SearchVO();
            when(adminMapper.countAllMembersBySearch(searchVO)).thenReturn(10);
            assertThat(adminService.countAllMembersBySearch(searchVO)).isEqualTo(10);
        }

        @Test
        @DisplayName("countAllTradesBySearch")
        void countAllTradesBySearch() {
            SearchVO searchVO = new SearchVO();
            when(adminMapper.countAllTradesBySearch(searchVO)).thenReturn(20);
            assertThat(adminService.countAllTradesBySearch(searchVO)).isEqualTo(20);
        }

        @Test
        @DisplayName("countAllSafePayListBySearch")
        void countAllSafePayListBySearch() {
            SearchVO searchVO = new SearchVO();
            when(adminMapper.countAllSafePayListBySearch(searchVO)).thenReturn(5);
            assertThat(adminService.countAllSafePayListBySearch(searchVO)).isEqualTo(5);
        }

        @Test
        @DisplayName("countAllGroupsBySearch")
        void countAllGroupsBySearch() {
            SearchVO searchVO = new SearchVO();
            when(adminMapper.countAllGroupsBySearch(searchVO)).thenReturn(3);
            assertThat(adminService.countAllGroupsBySearch(searchVO)).isEqualTo(3);
        }

        @Test
        @DisplayName("countAllNoticesBySearch")
        void countAllNoticesBySearch() {
            SearchVO searchVO = new SearchVO();
            when(adminMapper.countAllNoticesBySearch(searchVO)).thenReturn(15);
            assertThat(adminService.countAllNoticesBySearch(searchVO)).isEqualTo(15);
        }

        @Test
        @DisplayName("countActiveNotices")
        void countActiveNotices() {
            SearchVO searchVO = new SearchVO();
            when(adminMapper.countActiveNotices(searchVO)).thenReturn(8);
            assertThat(adminService.countActiveNotices(searchVO)).isEqualTo(8);
        }

        @Test
        @DisplayName("countAdminLoginLogsBySearch")
        void countAdminLoginLogsBySearch() {
            SearchVO searchVO = new SearchVO();
            when(adminMapper.countAdminLoginLogsBySearch(searchVO)).thenReturn(50);
            assertThat(adminService.countAdminLoginLogsBySearch(searchVO)).isEqualTo(50);
        }

        @Test
        @DisplayName("countUsersLoginLogsBySearch")
        void countUsersLoginLogsBySearch() {
            SearchVO searchVO = new SearchVO();
            when(adminMapper.countUsersLoginLogsBySearch(searchVO)).thenReturn(200);
            assertThat(adminService.countUsersLoginLogsBySearch(searchVO)).isEqualTo(200);
        }
    }
}
