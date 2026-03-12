package project.settlement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import project.member.MemberVO;
import project.trade.TradeService;
import project.util.exception.ClientException;
import project.util.exception.ForbiddenException;
import project.util.exception.settlement.SettlementException;
import project.settlement.SettlementStatus;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SettlementController 단위 테스트 (MockMvc standaloneSetup)
 * - 정산 신청 / 정산 상세 조회 엔드포인트 검증
 * - 세션 없음 / 정상 / 예외 케이스를 모두 커버한다
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementController")
class SettlementControllerTest {

    @Mock SettlementService settlementService;
    @Mock TradeService tradeService;

    @InjectMocks
    SettlementController settlementController;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(settlementController).build();
    }

    // ========== 헬퍼 ==========

    private MockHttpSession loginSession(long memberSeq) {
        MemberVO member = new MemberVO();
        member.setMember_seq(memberSeq);
        member.setLogin_id("seller" + memberSeq);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginSess", member);
        return session;
    }

    // ========================================================================
    // POST /settlement/request/{trade_seq} - 정산 신청
    // ========================================================================
    @Nested
    @DisplayName("POST /settlement/request/{trade_seq} - 정산 신청")
    class RequestSettlement {

        @Test
        @DisplayName("정산 신청 성공 - success: true + 메시지 포함")
        void success_returnsTrue() throws Exception {
            when(settlementService.requestSettlement(10L, 1L)).thenReturn(true);

            mockMvc.perform(post("/settlement/request/10")
                            .session(loginSession(1L)))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"success\":true}"));
        }

        @Test
        @DisplayName("세션 없음 - success: false + 로그인 필요 메시지")
        void noSession_returnsFalse() throws Exception {
            mockMvc.perform(post("/settlement/request/10"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"success\":false}"))
                    .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
        }

        @Test
        @DisplayName("SettlementException (이미 신청됨) - success: false + 예외 메시지")
        void settlementException_returnsFalse() throws Exception {
            when(settlementService.requestSettlement(10L, 1L))
                    .thenThrow(new SettlementException("이미 정산 신청되었거나 정산이 완료된 거래입니다."));

            mockMvc.perform(post("/settlement/request/10")
                            .session(loginSession(1L)))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"success\":false}"))
                    .andExpect(jsonPath("$.message").value("이미 정산 신청되었거나 정산이 완료된 거래입니다."));
        }

        @Test
        @DisplayName("ForbiddenException (비판매자) - success: false + 예외 메시지")
        void forbiddenException_returnsFalse() throws Exception {
            when(settlementService.requestSettlement(10L, 1L))
                    .thenThrow(new ForbiddenException("판매자만 정산 신청할 수 있습니다."));

            mockMvc.perform(post("/settlement/request/10")
                            .session(loginSession(1L)))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"success\":false}"));
        }

        @Test
        @DisplayName("서비스 반환 false - success: false")
        void serviceReturnsFalse_returnsFalse() throws Exception {
            when(settlementService.requestSettlement(10L, 1L)).thenReturn(false);

            mockMvc.perform(post("/settlement/request/10")
                            .session(loginSession(1L)))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"success\":false}"));
        }
    }

    // ========================================================================
    // GET /settlement/{trade_seq} - 정산 상세 조회
    // ========================================================================
    @Nested
    @DisplayName("GET /settlement/{trade_seq} - 정산 상세 조회")
    class GetSettlementDetail {

        @Test
        @DisplayName("세션 없음 - success: false")
        void noSession_returnsFalse() throws Exception {
            mockMvc.perform(get("/settlement/10"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"success\":false}"));
        }

        @Test
        @DisplayName("정산 내역 존재 - success: true + settlement 포함")
        void found_returnsSettlement() throws Exception {
            SettlementVO settlement = new SettlementVO();
            settlement.setSettlement_seq(1L);
            settlement.setSettlement_amount(12870);
            settlement.setSettlement_st(SettlementStatus.REQUESTED);

            when(settlementService.findByTradeSeq(10L)).thenReturn(settlement);

            mockMvc.perform(get("/settlement/10").session(loginSession(1L)))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"success\":true}"))
                    .andExpect(jsonPath("$.settlement.settlement_amount").value(12870));
        }

        @Test
        @DisplayName("정산 내역 없음 - success: false")
        void notFound_returnsFalse() throws Exception {
            when(settlementService.findByTradeSeq(10L)).thenReturn(null);

            mockMvc.perform(get("/settlement/10").session(loginSession(1L)))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"success\":false}"));
        }
    }
}
