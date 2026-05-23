package project.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import project.bookclub.BookClubService;
import project.member.MemberService;
import project.payment.PaymentEventService;
import project.payment.PaymentEventType;
import project.payment.PaymentEventVO;
import project.settlement.SettlementService;
import project.trade.TradeService;
import project.util.logInOut.LogoutPendingManager;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController payment incidents")
class AdminPaymentIncidentControllerTest {

    @Mock AdminService adminService;
    @Mock MemberService memberService;
    @Mock BookClubService bookClubService;
    @Mock TradeService tradeService;
    @Mock LogoutPendingManager logoutPendingManager;
    @Mock SettlementService settlementService;
    @Mock PaymentEventService paymentEventService;

    @InjectMocks AdminController adminController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    @DisplayName("관리자는 민감한 결제키 없이 확인 필요한 결제를 조회한다")
    void getPaymentIncidents_excludesPaymentKey() throws Exception {
        PaymentEventVO event = new PaymentEventVO();
        event.setTrade_seq(100L);
        event.setMember_seq(2L);
        event.setEvent_type(PaymentEventType.RECONCILE_REQUIRED);
        event.setOrder_id("order-100");
        event.setPayment_key("secret-payment-key");
        event.setToss_code("UNKNOWN_ERROR");
        when(paymentEventService.findUnresolvedConfirmUnknownEvents())
                .thenReturn(Collections.singletonList(event));

        mockMvc.perform(get("/admin/api/payment-incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.list[0].tradeSeq").value(100))
                .andExpect(jsonPath("$.list[0].eventType").value("RECONCILE_REQUIRED"))
                .andExpect(jsonPath("$.list[0].paymentKey").doesNotExist());

        verify(paymentEventService).findUnresolvedConfirmUnknownEvents();
    }
}
