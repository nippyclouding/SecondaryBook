package project.payment;

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
import project.chat.chatroom.ChatroomService;
import project.chat.message.MessageService;
import project.chat.pubsub.ChatMessagePublisher;
import project.member.MemberVO;
import project.trade.ENUM.SafePaymentStatus;
import project.trade.ENUM.SaleStatus;
import project.trade.TradeService;
import project.trade.TradeVO;
import project.util.Const;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentController")
class PaymentControllerTest {

    @Mock PaymentService paymentService;
    @Mock TradeService tradeService;
    @Mock TossApiService tossApiService;
    @Mock MessageService messageService;
    @Mock ChatMessagePublisher chatMessagePublisher;
    @Mock ChatroomService chatroomService;

    @InjectMocks PaymentController paymentController;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
    }

    private MockHttpSession sessionWith(long memberSeq) {
        MockHttpSession session = new MockHttpSession();
        MemberVO member = new MemberVO();
        member.setMember_seq(memberSeq);
        member.setMember_nicknm("buyer" + memberSeq);
        session.setAttribute(Const.SESSION, member);
        return session;
    }

    private TradeVO trade(long tradeSeq) {
        TradeVO trade = new TradeVO();
        trade.setTrade_seq(tradeSeq);
        trade.setMember_seller_seq(1L);
        trade.setSale_st(SaleStatus.SALE);
        trade.setSafe_payment_st(SafePaymentStatus.PENDING);
        trade.setSale_price(10000);
        trade.setDelivery_cost(3000);
        return trade;
    }

    private PaymentVO paymentCheck(Long pendingBuyerSeq) {
        return paymentCheck(pendingBuyerSeq, 120L);
    }

    private PaymentVO paymentCheck(Long pendingBuyerSeq, long remainingSeconds) {
        PaymentVO payment = new PaymentVO();
        payment.setSafe_payment_status(SafePaymentStatus.PENDING.name());
        payment.setPending_buyer_seq(pendingBuyerSeq);
        payment.setRemaining_seconds(remainingSeconds);
        return payment;
    }

    @Nested
    @DisplayName("GET /payments/fail - 결제 실패")
    class PaymentFail {

        @Test
        @DisplayName("현재 결제 구매자 실패 - PENDING을 NONE으로 복구하고 실패 메시지를 전송한다")
        void currentBuyerFailure_cancelsPendingAndSendsMessage() throws Exception {
            // given
            when(tradeService.search(100L)).thenReturn(trade(100L));
            when(chatroomService.isBuyerOfTrade(100L, 2L)).thenReturn(true);
            when(tradeService.getPaymentCheckInfo(100L)).thenReturn(paymentCheck(2L));
            when(tradeService.cancelSafePaymentForBuyer(100L, 2L)).thenReturn(true);
            when(chatroomService.findChatRoomSeqByTradeAndBuyer(100L, 2L)).thenReturn(10L);

            // when & then
            mockMvc.perform(get("/payments/fail")
                            .session(sessionWith(2L))
                            .param("trade_seq", "100")
                            .param("code", "PAY_PROCESS_CANCELED")
                            .param("message", "결제가 취소되었습니다."))
                    .andExpect(status().isOk())
                    .andExpect(view().name("payment/fail"))
                    .andExpect(model().attribute("errorCode", "PAY_PROCESS_CANCELED"))
                    .andExpect(model().attribute("errorMessage", "결제가 취소되었습니다."));

            verify(tradeService).cancelSafePaymentForBuyer(100L, 2L);
            verify(messageService).saveMessage(any());
            verify(chatMessagePublisher).publishPayment(eq(10L), any());
        }

        @Test
        @DisplayName("현재 결제 구매자가 아닌 실패 요청 - PENDING을 변경하지 않고 메시지도 전송하지 않는다")
        void otherBuyerFailure_doesNotCancelPending() throws Exception {
            // given
            when(tradeService.search(100L)).thenReturn(trade(100L));
            when(chatroomService.isBuyerOfTrade(100L, 2L)).thenReturn(true);
            when(tradeService.getPaymentCheckInfo(100L)).thenReturn(paymentCheck(3L));

            // when & then
            mockMvc.perform(get("/payments/fail")
                            .session(sessionWith(2L))
                            .param("trade_seq", "100")
                            .param("message", "결제 실패"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("payment/fail"));

            verify(tradeService, never()).cancelSafePaymentForBuyer(100L, 2L);
            verify(messageService, never()).saveMessage(any());
            verify(chatMessagePublisher, never()).publishPayment(anyLong(), any());
        }
    }

    @Nested
    @DisplayName("POST /payments/cancel - 결제 취소")
    class PaymentCancel {

        @Test
        @DisplayName("현재 결제 구매자 취소 - PENDING을 NONE으로 복구한다")
        void currentBuyerCancel_cancelsPending() throws Exception {
            // given
            when(tradeService.search(100L)).thenReturn(trade(100L));
            when(chatroomService.isBuyerOfTrade(100L, 2L)).thenReturn(true);
            when(tradeService.getPaymentCheckInfo(100L)).thenReturn(paymentCheck(2L));
            when(tradeService.cancelSafePaymentForBuyer(100L, 2L)).thenReturn(true);
            when(chatroomService.findChatRoomSeqByTradeAndBuyer(100L, 2L)).thenReturn(10L);

            // when & then
            mockMvc.perform(post("/payments/cancel")
                            .session(sessionWith(2L))
                            .param("trade_seq", "100"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"success\":true,\"message\":\"결제가 취소되었습니다.\"}"));

            verify(tradeService).cancelSafePaymentForBuyer(100L, 2L);
            verify(messageService).saveMessage(any());
            verify(chatMessagePublisher).publishPayment(eq(10L), any());
        }

        @Test
        @DisplayName("현재 결제 구매자가 아닌 취소 - PENDING을 변경하지 않는다")
        void otherBuyerCancel_doesNotCancelPending() throws Exception {
            // given
            when(tradeService.search(100L)).thenReturn(trade(100L));
            when(chatroomService.isBuyerOfTrade(100L, 2L)).thenReturn(true);
            when(tradeService.getPaymentCheckInfo(100L)).thenReturn(paymentCheck(3L));

            // when & then
            mockMvc.perform(post("/payments/cancel")
                            .session(sessionWith(2L))
                            .param("trade_seq", "100"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"success\":false,\"message\":\"진행 중인 결제를 찾을 수 없습니다.\"}"));

            verify(tradeService, never()).cancelSafePaymentForBuyer(100L, 2L);
            verify(messageService, never()).saveMessage(any());
        }
    }

    @Nested
    @DisplayName("POST /payments/timeout - 결제 시간 만료")
    class PaymentTimeout {

        @Test
        @DisplayName("현재 결제 구매자 타임아웃 - PENDING을 NONE으로 복구한다")
        void currentBuyerTimeout_cancelsPending() throws Exception {
            // given
            when(tradeService.search(100L)).thenReturn(trade(100L));
            when(chatroomService.isBuyerOfTrade(100L, 2L)).thenReturn(true);
            when(tradeService.getPaymentCheckInfo(100L)).thenReturn(paymentCheck(2L));
            when(tradeService.cancelSafePaymentForBuyer(100L, 2L)).thenReturn(true);
            when(chatroomService.findChatRoomSeqByTradeAndBuyer(100L, 2L)).thenReturn(10L);

            // when & then
            mockMvc.perform(post("/payments/timeout")
                            .session(sessionWith(2L))
                            .param("trade_seq", "100"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"success\":true,\"message\":\"결제 시간이 만료되었습니다.\"}"));

            verify(tradeService).cancelSafePaymentForBuyer(100L, 2L);
            verify(messageService).saveMessage(any());
            verify(chatMessagePublisher).publishPayment(eq(10L), any());
        }

        @Test
        @DisplayName("현재 결제 구매자가 아닌 타임아웃 - PENDING을 변경하지 않는다")
        void otherBuyerTimeout_doesNotCancelPending() throws Exception {
            // given
            when(tradeService.search(100L)).thenReturn(trade(100L));
            when(chatroomService.isBuyerOfTrade(100L, 2L)).thenReturn(true);
            when(tradeService.getPaymentCheckInfo(100L)).thenReturn(paymentCheck(3L));

            // when & then
            mockMvc.perform(post("/payments/timeout")
                            .session(sessionWith(2L))
                            .param("trade_seq", "100"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"success\":false,\"message\":\"진행 중인 결제를 찾을 수 없습니다.\"}"));

            verify(tradeService, never()).cancelSafePaymentForBuyer(100L, 2L);
            verify(messageService, never()).saveMessage(any());
        }
    }

    @Nested
    @DisplayName("GET /payments/success - 승인 실패")
    class PaymentSuccessFailure {

        @Test
        @DisplayName("결제 시간이 만료된 성공 콜백 - 토스 승인 전에 PENDING을 NONE으로 복구한다")
        void expiredSuccessCallback_cancelsBeforeTossConfirm() throws Exception {
            // given
            when(tradeService.search(100L)).thenReturn(trade(100L));
            when(tradeService.getPaymentCheckInfo(100L)).thenReturn(paymentCheck(2L, 0L));
            when(tradeService.cancelSafePaymentForBuyer(100L, 2L)).thenReturn(true);
            when(chatroomService.findChatRoomSeqByTradeAndBuyer(100L, 2L)).thenReturn(10L);

            // when & then
            mockMvc.perform(get("/payments/success")
                            .session(sessionWith(2L))
                            .param("trade_seq", "100")
                            .param("paymentKey", "payment-key")
                            .param("orderId", "order-id")
                            .param("amount", "13000")
                            .param("addr_type", "direct"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/payments/result?status=fail"))
                    .andExpect(flash().attribute("errorMessage", "결제 시간이 만료되었습니다. 다시 시도해주세요."));

            verify(tossApiService, never()).confirmPayment(anyString(), anyString(), anyInt());
            verify(tradeService).cancelSafePaymentForBuyer(100L, 2L);
            verify(messageService).saveMessage(any());
            verify(chatMessagePublisher).publishPayment(eq(10L), any());
        }

        @Test
        @DisplayName("토스 승인 실패 - PENDING을 NONE으로 복구하고 실패 결과로 보낸다")
        void tossConfirmFailure_cancelsPendingAndRedirectsFail() throws Exception {
            // given
            TossPaymentResponse tossResponse = new TossPaymentResponse();
            tossResponse.setStatus("FAILED");
            tossResponse.setMessage("결제 승인 실패");

            when(tradeService.search(100L)).thenReturn(trade(100L));
            when(tossApiService.confirmPayment("payment-key", "order-id", 13000)).thenReturn(tossResponse);
            when(tradeService.getPaymentCheckInfo(100L)).thenReturn(paymentCheck(2L));
            when(tradeService.cancelSafePaymentForBuyer(100L, 2L)).thenReturn(true);
            when(chatroomService.findChatRoomSeqByTradeAndBuyer(100L, 2L)).thenReturn(10L);

            // when & then
            mockMvc.perform(get("/payments/success")
                            .session(sessionWith(2L))
                            .param("trade_seq", "100")
                            .param("paymentKey", "payment-key")
                            .param("orderId", "order-id")
                            .param("amount", "13000")
                            .param("addr_type", "direct"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/payments/result?status=fail"))
                    .andExpect(flash().attribute("errorMessage", "결제 승인 실패"));

            verify(tradeService).cancelSafePaymentForBuyer(100L, 2L);
            verify(messageService).saveMessage(any());
            verify(chatMessagePublisher).publishPayment(eq(10L), any());
        }
    }
}
