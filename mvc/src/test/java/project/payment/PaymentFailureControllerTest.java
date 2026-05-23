package project.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import project.chat.chatroom.ChatroomService;
import project.member.MemberVO;
import project.trade.ENUM.SafePaymentStatus;
import project.trade.ENUM.SaleStatus;
import project.trade.TradeService;
import project.trade.TradeVO;
import project.util.Const;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentController failure callback")
class PaymentFailureControllerTest {

    private static final LocalDateTime ATTEMPT_EXPIRES_AT = LocalDateTime.of(2026, 5, 23, 14, 5);
    private static final String ATTEMPT = "2026-05-23T14:05";
    private static final String NEW_ATTEMPT = "2026-05-23T14:10";

    @Mock PaymentService paymentService;
    @Mock TradeService tradeService;
    @Mock ChatroomService chatroomService;
    @Mock SafePaymentService safePaymentService;

    @InjectMocks PaymentController paymentController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentController, "tossClientKey", "configured-client-key");
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
    }

    @Test
    @DisplayName("토스 실패 리다이렉트는 렌더링 전에 PENDING 해제를 처리한다")
    void tossFailureRedirect_releasesPendingBeforeRendering() throws Exception {
        MockHttpSession session = sessionWith(2L);
        session.setAttribute("paymentFailureToken:100:" + ATTEMPT, "failure-token");
        when(tradeService.search(100L)).thenReturn(pendingTrade());
        when(chatroomService.isBuyerOfTrade(100L, 2L)).thenReturn(true);
        when(safePaymentService.failPendingPayment(100L, 2L, PaymentEventType.TOSS_FAIL, ATTEMPT_EXPIRES_AT))
                .thenReturn(SafePaymentService.Result.success("결제에 실패했습니다."));

        mockMvc.perform(get("/payments/fail")
                        .session(session)
                        .param("trade_seq", "100")
                        .param("reason", "TOSS_FAIL")
                        .param("failure_token", "failure-token")
                        .param("failure_attempt", ATTEMPT))
                .andExpect(status().isOk())
                .andExpect(view().name("payment/fail"));

        verify(safePaymentService).failPendingPayment(
                100L, 2L, PaymentEventType.TOSS_FAIL, ATTEMPT_EXPIRES_AT);
    }

    @Test
    @DisplayName("실패 토큰이 없는 GET 요청은 진행 중 결제를 취소하지 않는다")
    void failureRedirectWithoutToken_doesNotReleasePayment() throws Exception {
        when(tradeService.search(100L)).thenReturn(pendingTrade());
        when(chatroomService.isBuyerOfTrade(100L, 2L)).thenReturn(true);

        mockMvc.perform(get("/payments/fail")
                        .session(sessionWith(2L))
                        .param("trade_seq", "100")
                        .param("reason", "TOSS_FAIL")
                        .param("failure_attempt", ATTEMPT))
                .andExpect(status().isOk())
                .andExpect(view().name("payment/fail"));

        verify(safePaymentService, never()).failPendingPayment(
                100L, 2L, PaymentEventType.TOSS_FAIL, ATTEMPT_EXPIRES_AT);
    }

    @Test
    @DisplayName("거래 구매자가 아닌 실패 리다이렉트는 상태를 변경하지 않는다")
    void failureRedirectByOtherMember_doesNotReleasePayment() throws Exception {
        when(tradeService.search(100L)).thenReturn(pendingTrade());
        when(chatroomService.isBuyerOfTrade(100L, 3L)).thenReturn(false);

        mockMvc.perform(get("/payments/fail")
                        .session(sessionWith(3L))
                        .param("trade_seq", "100")
                        .param("reason", "TOSS_FAIL")
                        .param("failure_attempt", ATTEMPT))
                .andExpect(status().is3xxRedirection());

        verify(safePaymentService, never()).failPendingPayment(
                100L, 3L, PaymentEventType.TOSS_FAIL, ATTEMPT_EXPIRES_AT);
    }

    @Test
    @DisplayName("같은 결제 페이지를 다시 열어도 기존 실패 토큰을 재사용한다")
    void showPayment_reusesFailureTokenForSamePendingAttempt() throws Exception {
        MockHttpSession session = sessionWith(2L);
        TradeVO trade = new TradeVO();
        trade.setMember_seller_seq(1L);
        trade.setSale_st(SaleStatus.SALE);
        trade.setSafe_payment_expire_dtm(ATTEMPT_EXPIRES_AT);
        PaymentVO payment = new PaymentVO();
        payment.setSafe_payment_status(SafePaymentStatus.PENDING.name());
        payment.setPending_buyer_seq(2L);
        when(tradeService.search(100L)).thenReturn(trade);
        when(tradeService.getPaymentCheckInfo(100L)).thenReturn(payment);

        String firstToken = (String) mockMvc.perform(get("/payments")
                .session(session)
                        .param("trade_seq", "100"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment/payform"))
                .andExpect(model().attribute("tossClientKey", "configured-client-key"))
                .andReturn().getModelAndView().getModel().get("failureToken");
        String secondToken = (String) mockMvc.perform(get("/payments")
                        .session(session)
                        .param("trade_seq", "100"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment/payform"))
                .andReturn().getModelAndView().getModel().get("failureToken");

        assertThat(firstToken).isNotBlank();
        assertThat(secondToken).isEqualTo(firstToken);
        assertThat(session.getAttribute("paymentFailureToken:100:" + ATTEMPT)).isEqualTo(firstToken);
    }

    @Test
    @DisplayName("페이지 이탈 해제가 성공하면 실패 토큰을 폐기한다")
    void pageLeaveSuccess_removesFailureToken() throws Exception {
        MockHttpSession session = sessionWith(2L);
        session.setAttribute("paymentFailureToken:100:" + ATTEMPT, "failure-token");
        when(safePaymentService.failPendingPayment(100L, 2L, PaymentEventType.PAGE_LEAVE, ATTEMPT_EXPIRES_AT))
                .thenReturn(SafePaymentService.Result.success("해제되었습니다."));

        mockMvc.perform(post("/payments/failure")
                        .session(session)
                        .param("trade_seq", "100")
                        .param("reason", "PAGE_LEAVE")
                        .param("failure_token", "failure-token")
                        .param("failure_attempt", ATTEMPT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(session.getAttribute("paymentFailureToken:100:" + ATTEMPT)).isNull();
    }

    @Test
    @DisplayName("이전 결제 시도의 실패 리다이렉트는 새 결제를 해제하지 않는다")
    void staleFailureAttempt_doesNotReleaseNewPendingPayment() throws Exception {
        MockHttpSession session = sessionWith(2L);
        session.setAttribute("paymentFailureToken:100:" + ATTEMPT, "failure-token");
        TradeVO newAttempt = pendingTrade();
        newAttempt.setSafe_payment_expire_dtm(ATTEMPT_EXPIRES_AT.plusMinutes(5));
        when(tradeService.search(100L)).thenReturn(newAttempt);
        when(chatroomService.isBuyerOfTrade(100L, 2L)).thenReturn(true);

        mockMvc.perform(get("/payments/fail")
                        .session(session)
                        .param("trade_seq", "100")
                        .param("reason", "TOSS_FAIL")
                        .param("failure_token", "failure-token")
                        .param("failure_attempt", ATTEMPT))
                .andExpect(status().isOk())
                .andExpect(view().name("payment/fail"));

        verify(safePaymentService, never()).failPendingPayment(
                100L, 2L, PaymentEventType.TOSS_FAIL, ATTEMPT_EXPIRES_AT);
    }

    @Test
    @DisplayName("이전 성공 콜백은 새 결제 시도의 실패 토큰을 제거하지 않는다")
    void staleSuccessCallback_preservesNewPendingFailureToken() throws Exception {
        MockHttpSession session = sessionWith(2L);
        session.setAttribute("paymentFailureToken:100:" + ATTEMPT, "old-token");
        session.setAttribute("paymentFailureToken:100:" + NEW_ATTEMPT, "new-token");
        when(safePaymentService.confirm(any(SafePaymentService.ConfirmRequest.class)))
                .thenReturn(SafePaymentService.Result.failure(PaymentErrorCode.PAYMENT_UNAVAILABLE));

        mockMvc.perform(get("/payments/success")
                        .session(session)
                        .param("trade_seq", "100")
                        .param("paymentKey", "old-payment-key")
                        .param("orderId", "old-order-id")
                        .param("amount", "13000")
                        .param("addr_type", "direct")
                        .param("failure_attempt", ATTEMPT))
                .andExpect(status().is3xxRedirection());

        assertThat(session.getAttribute("paymentFailureToken:100:" + ATTEMPT)).isNull();
        assertThat(session.getAttribute("paymentFailureToken:100:" + NEW_ATTEMPT)).isEqualTo("new-token");
    }

    private TradeVO pendingTrade() {
        TradeVO trade = new TradeVO();
        trade.setSafe_payment_expire_dtm(ATTEMPT_EXPIRES_AT);
        return trade;
    }

    private MockHttpSession sessionWith(long memberSeq) {
        MockHttpSession session = new MockHttpSession();
        MemberVO member = new MemberVO();
        member.setMember_seq(memberSeq);
        session.setAttribute(Const.SESSION, member);
        return session;
    }
}
