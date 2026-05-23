package project.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.reactive.function.client.WebClient;
import project.admin.AdminService;
import project.bookclub.BookClubService;
import project.util.logInOut.LogoutPendingManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberController - 인증 상태 변경")
class MemberControllerTest {

    @Mock MemberService memberService;
    @Mock MailService mailService;
    @Mock AdminService adminService;
    @Mock LogoutPendingManager logoutPendingManager;
    @Mock BookClubService bookClubService;
    @Mock ObjectMapper objectMapper;
    @Mock WebClient kakaoAuthWebClient;
    @Mock WebClient kakaoApiWebClient;
    @Mock WebClient naverAuthWebClient;
    @Mock WebClient naverApiWebClient;

    @InjectMocks
    MemberController memberController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(memberController).build();
    }

    @Test
    @DisplayName("일반 로그인 성공 시 로그인 세션을 만들고 이동한다")
    void loginSuccess_setsSession() throws Exception {
        MemberVO member = member(1L, "testUser");
        when(memberService.login(any())).thenReturn(member);

        mockMvc.perform(post("/login")
                        .param("login_id", "testUser")
                        .param("member_pwd", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(request -> {
                    Object loggedIn = request.getRequest().getSession().getAttribute("loginSess");
                    org.assertj.core.api.Assertions.assertThat(loggedIn).isSameAs(member);
                });
    }

    @Test
    @DisplayName("비밀번호 재설정은 인증 완료 세션과 동일 아이디에서만 실행된다")
    void resetPassword_verifiedSessionExecutesService() throws Exception {
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
    @DisplayName("비밀번호 재설정 인증 세션이 없으면 거부한다")
    void resetPassword_withoutVerificationRejected() throws Exception {
        mockMvc.perform(post("/auth/ajax/resetPassword")
                        .param("login_id", "testUser")
                        .param("new_pwd", "newPwd123"))
                .andExpect(status().isOk())
                .andExpect(content().string("fail"));
    }

    @Test
    @DisplayName("회원 탈퇴 성공 시 완료 화면으로 이동한다")
    void deleteMember_successReturnsCompletion() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginSess", member(1L, "testUser"));
        when(memberService.deleteMember(1L)).thenReturn(true);

        mockMvc.perform(post("/member/delete").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("common/return"))
                .andExpect(model().attribute("url", "/"));
    }

    private MemberVO member(long seq, String loginId) {
        MemberVO member = new MemberVO();
        member.setMember_seq(seq);
        member.setLogin_id(loginId);
        return member;
    }
}
