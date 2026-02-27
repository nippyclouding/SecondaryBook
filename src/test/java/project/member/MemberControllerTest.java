package project.member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.reactive.function.client.WebClient;
import project.admin.AdminService;
import project.bookclub.BookClubService;
import project.util.logInOut.LogoutPendingManager;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MemberController 단위 테스트 (MockMvc standaloneSetup)
 * - Spring 컨텍스트 로딩 없이 컨트롤러 레이어만 독립 검증
 * - 세션·모델·응답 본문·리다이렉트를 검증한다
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MemberController")
class MemberControllerTest {

    @Mock MemberService memberService;
    @Mock MailService mailService;
    @Mock AdminService adminService;
    @Mock LogoutPendingManager logoutPendingManager;
    @Mock BookClubService bookClubService;
    @Mock WebClient kakaoAuthWebClient;
    @Mock WebClient kakaoApiWebClient;
    @Mock WebClient naverAuthWebClient;
    @Mock WebClient naverApiWebClient;

    @InjectMocks
    MemberController memberController;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // @Value 필드 주입 (Spring 컨텍스트 없이)
        ReflectionTestUtils.setField(memberController, "kakaoClientId",    "test-kakao-id");
        ReflectionTestUtils.setField(memberController, "kakaoRedirectUri", "http://localhost/kakao/callback");
        ReflectionTestUtils.setField(memberController, "kakaoSecretCode",  "kakao-secret");
        ReflectionTestUtils.setField(memberController, "naverClientId",    "test-naver-id");
        ReflectionTestUtils.setField(memberController, "naverClientSecret","naver-secret");
        ReflectionTestUtils.setField(memberController, "naverRedirectUri", "http://localhost/naver/callback");

        mockMvc = MockMvcBuilders.standaloneSetup(memberController).build();
    }

    // ========== 헬퍼 ==========

    private MemberVO loginMember(long seq, String loginId) {
        MemberVO m = new MemberVO();
        m.setMember_seq(seq);
        m.setLogin_id(loginId);
        m.setMember_nicknm("테스터");
        return m;
    }

    // ========================================================================
    // GET /login - 로그인 페이지
    // ========================================================================
    @Nested
    @DisplayName("GET /login - 로그인 페이지")
    class LoginPage {

        @Test
        @DisplayName("카카오·네이버 클라이언트 ID를 모델에 담아 login 뷰를 반환한다")
        void showsLoginPageWithOAuthIds() throws Exception {
            mockMvc.perform(get("/login"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/login"))
                    .andExpect(model().attribute("kakaoClientId", "test-kakao-id"))
                    .andExpect(model().attribute("naverClientId", "test-naver-id"));
        }

        @Test
        @DisplayName("redirect 파라미터가 유효하면 모델에 담긴다")
        void validRedirect_addedToModel() throws Exception {
            mockMvc.perform(get("/login").param("redirect", "/mypage"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("redirect"));
        }
    }

    // ========================================================================
    // POST /login - 로그인 처리
    // ========================================================================
    @Nested
    @DisplayName("POST /login - 로그인 처리")
    class Login {

        @Test
        @DisplayName("로그인 성공 - 루트로 리다이렉트")
        void success_redirectsToRoot() throws Exception {
            MemberVO member = loginMember(1L, "testUser");
            when(memberService.login(any())).thenReturn(member);
            when(memberService.loginLogUpdate(1L)).thenReturn(true);

            mockMvc.perform(post("/login")
                            .param("login_id", "testUser")
                            .param("member_pwd", "password123"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/"));
        }

        @Test
        @DisplayName("로그인 실패 - 오류 메시지 + common/return 뷰")
        void failure_showsErrorMessage() throws Exception {
            when(memberService.login(any())).thenReturn(null);

            mockMvc.perform(post("/login")
                            .param("login_id", "wrongUser")
                            .param("member_pwd", "wrongPwd"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("common/return"))
                    .andExpect(model().attributeExists("msg"));
        }
    }

    // ========================================================================
    // GET /signup - 회원가입 페이지
    // ========================================================================
    @Test
    @DisplayName("GET /signup - signup 뷰를 반환한다")
    void signupPage_returnsSignupView() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/signup"));
    }

    // ========================================================================
    // GET /auth/ajax/idCheck
    // ========================================================================
    @Nested
    @DisplayName("GET /auth/ajax/idCheck - 아이디 중복 확인")
    class IdCheck {

        @Test
        @DisplayName("중복 아이디 - 1 반환")
        void duplicated_returns1() throws Exception {
            when(memberService.idCheck("exist")).thenReturn(1);

            mockMvc.perform(get("/auth/ajax/idCheck").param("login_id", "exist"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("1"));
        }

        @Test
        @DisplayName("사용 가능 아이디 - 0 반환")
        void available_returns0() throws Exception {
            when(memberService.idCheck("newUser")).thenReturn(0);

            mockMvc.perform(get("/auth/ajax/idCheck").param("login_id", "newUser"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("0"));
        }
    }

    // ========================================================================
    // GET /auth/ajax/emailCheck
    // ========================================================================
    @Test
    @DisplayName("GET /auth/ajax/emailCheck - 중복 이메일 1 반환")
    void emailCheck_duplicated() throws Exception {
        when(memberService.emailCheck("dup@test.com")).thenReturn(1);

        mockMvc.perform(get("/auth/ajax/emailCheck").param("member_email", "dup@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    // ========================================================================
    // GET /auth/ajax/nicknmCheck
    // ========================================================================
    @Test
    @DisplayName("GET /auth/ajax/nicknmCheck - 사용 가능 닉네임 0 반환")
    void nicknmCheck_available() throws Exception {
        when(memberService.nickNmCheck("새닉네임")).thenReturn(0);

        mockMvc.perform(get("/auth/ajax/nicknmCheck").param("member_nicknm", "새닉네임"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    // ========================================================================
    // GET /api/session-check
    // ========================================================================
    @Nested
    @DisplayName("GET /api/session-check - 세션 확인")
    class SessionCheck {

        @Test
        @DisplayName("로그인된 세션 - loggedIn: true")
        void loggedIn_true() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("loginSess", loginMember(1L, "user"));

            mockMvc.perform(get("/api/session-check").session(session))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"loggedIn\":true}"));
        }

        @Test
        @DisplayName("비로그인 세션 - loggedIn: false")
        void notLoggedIn_false() throws Exception {
            mockMvc.perform(get("/api/session-check"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"loggedIn\":false}"));
        }
    }

    // ========================================================================
    // POST /auth/ajax/findId - 아이디 찾기
    // ========================================================================
    @Nested
    @DisplayName("POST /auth/ajax/findId - 전화번호로 아이디 찾기")
    class FindId {

        @Test
        @DisplayName("일치하는 회원 존재 - 아이디 반환")
        void found_returnsLoginId() throws Exception {
            when(memberService.findIdByTel("01012345678")).thenReturn("testUser");

            mockMvc.perform(post("/auth/ajax/findId").param("member_tel_no", "01012345678"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("testUser"));
        }

        @Test
        @DisplayName("일치 없음 - fail 반환")
        void notFound_returnsFail() throws Exception {
            when(memberService.findIdByTel("01099999999")).thenReturn(null);

            mockMvc.perform(post("/auth/ajax/findId").param("member_tel_no", "01099999999"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("fail"));
        }
    }

    // ========================================================================
    // POST /auth/ajax/resetPassword - 비밀번호 재설정
    // ========================================================================
    @Nested
    @DisplayName("POST /auth/ajax/resetPassword - 비밀번호 재설정")
    class ResetPassword {

        @Test
        @DisplayName("인증 완료 세션 + 아이디 일치 - 서비스 결과 반환")
        void verified_returnsServiceResult() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("pwdResetVerified", true);
            session.setAttribute("pwdResetLoginId", "testUser");

            when(memberService.resetPassword("testUser", "newPwd123")).thenReturn("success");

            mockMvc.perform(post("/auth/ajax/resetPassword")
                            .session(session)
                            .param("login_id", "testUser")
                            .param("new_pwd", "newPwd123"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("success"));
        }

        @Test
        @DisplayName("인증 세션 없음 - fail 반환")
        void noSession_returnsFail() throws Exception {
            mockMvc.perform(post("/auth/ajax/resetPassword")
                            .param("login_id", "testUser")
                            .param("new_pwd", "newPwd123"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("fail"));
        }

        @Test
        @DisplayName("아이디 불일치 - fail 반환")
        void loginIdMismatch_returnsFail() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("pwdResetVerified", true);
            session.setAttribute("pwdResetLoginId", "otherUser");

            mockMvc.perform(post("/auth/ajax/resetPassword")
                            .session(session)
                            .param("login_id", "testUser")
                            .param("new_pwd", "newPwd123"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("fail"));
        }
    }

    // ========================================================================
    // POST /member/delete - 회원 탈퇴
    // ========================================================================
    @Nested
    @DisplayName("POST /member/delete - 회원 탈퇴")
    class DeleteMember {

        @Test
        @DisplayName("탈퇴 성공 - 세션 만료 + 루트 이동 메시지")
        void success_invalidatesSession() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("loginSess", loginMember(1L, "user"));
            when(memberService.deleteMember(1L)).thenReturn(true);

            mockMvc.perform(post("/member/delete").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("common/return"))
                    .andExpect(model().attribute("url", "/"));
        }

        @Test
        @DisplayName("탈퇴 실패 - 오류 메시지")
        void failure_showsError() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("loginSess", loginMember(1L, "user"));
            when(memberService.deleteMember(1L)).thenReturn(false);

            mockMvc.perform(post("/member/delete").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("common/return"))
                    .andExpect(model().attributeExists("msg"));
        }
    }
}
