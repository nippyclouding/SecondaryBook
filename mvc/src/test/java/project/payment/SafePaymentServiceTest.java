package project.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.chat.chatroom.ChatroomService;
import project.chat.message.MessageService;
import project.chat.pubsub.ChatMessagePublisher;
import project.trade.ENUM.SafePaymentStatus;
import project.trade.ENUM.SaleStatus;
import project.trade.TradeService;
import project.trade.TradeVO;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SafePaymentService")
class SafePaymentServiceTest {

    private static final LocalDateTime ATTEMPT_EXPIRES_AT = LocalDateTime.of(2026, 5, 23, 14, 5);

    @Mock TradeService tradeService;
    @Mock TossApiService tossApiService;
    @Mock PaymentEventService paymentEventService;
    @Mock ChatroomService chatroomService;
    @Mock MessageService messageService;
    @Mock ChatMessagePublisher chatMessagePublisher;

    @InjectMocks SafePaymentService safePaymentService;

    @Test
    @DisplayName("안전결제 성공 시 승인 후 거래 완료와 SUCCESS 로그를 기록한다")
    void confirm_successCompletesTradeAndRecordsEvent() {
        TossPaymentResponse approved = response("DONE");
        when(tradeService.search(100L)).thenReturn(trade());
        when(tradeService.getPaymentCheckInfo(100L)).thenReturn(pending(), confirming());
        when(paymentEventService.beginConfirmation(100L, 2L, "pay-key", "order-id", 13000, ATTEMPT_EXPIRES_AT)).thenReturn(true);
        when(tossApiService.confirmPayment("pay-key", "order-id", 13000)).thenReturn(approved);

        SafePaymentService.Result result = safePaymentService.confirm(request());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPayment().getAmount()).isEqualTo(13000);
        verify(tradeService).completePurchaseAndNotify(100L, 2L,
                "직거래/반값택배", "직거래/반값택배", "직거래/반값택배");
        verify(paymentEventService).beginConfirmation(100L, 2L, "pay-key", "order-id", 13000, ATTEMPT_EXPIRES_AT);
        verify(paymentEventService).record(100L, 2L, PaymentEventType.SUCCESS,
                "pay-key", "order-id", 13000, approved);
    }

    @Test
    @DisplayName("토스 승인 실패 시 CONFIRMING을 해제하고 TOSS_FAIL 로그를 기록한다")
    void confirm_tossFailureReleasesConfirmation() {
        TossPaymentResponse failed = response("ABORTED");
        failed.setMessage("승인 거절");
        when(tradeService.search(100L)).thenReturn(trade());
        when(tradeService.getPaymentCheckInfo(100L)).thenReturn(pending());
        when(paymentEventService.beginConfirmation(100L, 2L, "pay-key", "order-id", 13000, ATTEMPT_EXPIRES_AT)).thenReturn(true);
        when(tossApiService.confirmPayment("pay-key", "order-id", 13000)).thenReturn(failed);
        when(tradeService.failConfirmingSafePaymentForBuyer(100L, 2L)).thenReturn(true);

        SafePaymentService.Result result = safePaymentService.confirm(request());

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo("PAYMENT_009");
        assertThat(result.getMessage()).isEqualTo("승인 거절");
        verify(tradeService).failConfirmingSafePaymentForBuyer(100L, 2L);
        verify(paymentEventService).record(100L, 2L, PaymentEventType.TOSS_FAIL,
                "pay-key", "order-id", 13000, failed);
    }

    @Test
    @DisplayName("승인 시작 로그 저장 실패 시 상태를 잠그거나 Toss 승인을 호출하지 않는다")
    void confirm_eventLogFailureStopsBeforeConfirmationLock() {
        when(tradeService.search(100L)).thenReturn(trade());
        when(tradeService.getPaymentCheckInfo(100L)).thenReturn(pending());
        when(tradeService.cancelSafePaymentForBuyerAndAttempt(100L, 2L, ATTEMPT_EXPIRES_AT)).thenReturn(true);
        doThrow(new RuntimeException("event insert failure")).when(paymentEventService)
                .beginConfirmation(100L, 2L, "pay-key", "order-id", 13000, ATTEMPT_EXPIRES_AT);

        SafePaymentService.Result result = safePaymentService.confirm(request());

        assertThat(result.getCode()).isEqualTo("PAYMENT_008");
        verify(tradeService).cancelSafePaymentForBuyerAndAttempt(100L, 2L, ATTEMPT_EXPIRES_AT);
        verify(tossApiService, never()).confirmPayment("pay-key", "order-id", 13000);
    }

    @Test
    @DisplayName("사용자가 결제 화면을 이탈하면 PENDING을 해제하고 PAGE_LEAVE 로그를 기록한다")
    void failPendingPayment_pageLeaveReleasesPending() {
        givenPendingFailure();

        SafePaymentService.Result result = safePaymentService.failPendingPayment(
                100L, 2L, PaymentEventType.PAGE_LEAVE, ATTEMPT_EXPIRES_AT);

        assertThat(result.isSuccess()).isTrue();
        verify(tradeService).cancelSafePaymentForBuyerAndAttempt(100L, 2L, ATTEMPT_EXPIRES_AT);
        verify(paymentEventService).record(100L, 2L, PaymentEventType.PAGE_LEAVE,
                null, null, null, null);
    }

    @Test
    @DisplayName("결제 화면 타임아웃이면 PENDING을 해제하고 TIMEOUT 로그를 기록한다")
    void failPendingPayment_timeoutReleasesPending() {
        givenPendingFailure();

        SafePaymentService.Result result = safePaymentService.failPendingPayment(
                100L, 2L, PaymentEventType.TIMEOUT, ATTEMPT_EXPIRES_AT);

        assertThat(result.getMessage()).isEqualTo("결제 시간이 만료되었습니다.");
        verify(paymentEventService).record(100L, 2L, PaymentEventType.TIMEOUT,
                null, null, null, null);
    }

    @Test
    @DisplayName("브라우저 실패 콜백은 같은 결제 시도일 때만 PENDING을 해제한다")
    void failPendingPayment_attemptBoundCallbackReleasesOnlyMatchingPending() {
        when(tradeService.search(100L)).thenReturn(trade());
        when(chatroomService.isBuyerOfTrade(100L, 2L)).thenReturn(true);
        PaymentVO pending = pending();
        pending.setSafe_payment_expire_dtm(ATTEMPT_EXPIRES_AT);
        when(tradeService.getPaymentCheckInfo(100L)).thenReturn(pending);
        when(tradeService.cancelSafePaymentForBuyerAndAttempt(100L, 2L, ATTEMPT_EXPIRES_AT))
                .thenReturn(true);

        SafePaymentService.Result result = safePaymentService.failPendingPayment(
                100L, 2L, PaymentEventType.PAGE_LEAVE, ATTEMPT_EXPIRES_AT);

        assertThat(result.isSuccess()).isTrue();
        verify(tradeService).cancelSafePaymentForBuyerAndAttempt(100L, 2L, ATTEMPT_EXPIRES_AT);
    }

    @Test
    @DisplayName("이전 결제 시도의 성공 콜백은 새 PENDING 승인으로 진입하지 않는다")
    void confirm_staleAttemptDoesNotStartConfirmation() {
        TradeVO currentAttempt = trade();
        currentAttempt.setSafe_payment_expire_dtm(ATTEMPT_EXPIRES_AT.plusMinutes(5));
        when(tradeService.search(100L)).thenReturn(currentAttempt);

        SafePaymentService.Result result = safePaymentService.confirm(new SafePaymentService.ConfirmRequest(
                100L, 2L, "pay-key", "order-id", 13000, "direct", null, null, null, ATTEMPT_EXPIRES_AT));

        assertThat(result.getCode()).isEqualTo("PAYMENT_004");
        verify(paymentEventService, never()).beginConfirmation(
                100L, 2L, "pay-key", "order-id", 13000, ATTEMPT_EXPIRES_AT);
        verify(tossApiService, never()).confirmPayment("pay-key", "order-id", 13000);
    }

    @Test
    @DisplayName("시도 식별값이 있는 승인 사전 검증 실패는 같은 PENDING만 해제한다")
    void confirm_amountMismatchUsesAttemptBoundRelease() {
        TradeVO matchingAttempt = trade();
        matchingAttempt.setSafe_payment_expire_dtm(ATTEMPT_EXPIRES_AT);
        PaymentVO pending = pending();
        pending.setSafe_payment_expire_dtm(ATTEMPT_EXPIRES_AT);
        when(tradeService.search(100L)).thenReturn(matchingAttempt);
        when(tradeService.getPaymentCheckInfo(100L)).thenReturn(pending);
        when(tradeService.cancelSafePaymentForBuyerAndAttempt(100L, 2L, ATTEMPT_EXPIRES_AT))
                .thenReturn(true);

        SafePaymentService.Result result = safePaymentService.confirm(new SafePaymentService.ConfirmRequest(
                100L, 2L, "pay-key", "order-id", 1, "direct", null, null, null, ATTEMPT_EXPIRES_AT));

        assertThat(result.getCode()).isEqualTo("PAYMENT_003");
        verify(tradeService).cancelSafePaymentForBuyerAndAttempt(100L, 2L, ATTEMPT_EXPIRES_AT);
    }

    @Test
    @DisplayName("5분이 지난 PENDING은 스케줄러 호출 대상 서비스가 해제하고 만료 로그를 기록한다")
    void cleanupExpiredSafePayments_releasesExpiredPending() {
        TradeVO expired = trade();
        expired.setPending_buyer_seq(2L);
        expired.setSafe_payment_expire_dtm(ATTEMPT_EXPIRES_AT);
        when(tradeService.findExpiredPendingSafePayments()).thenReturn(Collections.singletonList(expired));
        when(tradeService.cancelSafePaymentForBuyerAndAttempt(100L, 2L, ATTEMPT_EXPIRES_AT)).thenReturn(true);
        when(chatroomService.findChatRoomSeqByTradeAndBuyer(100L, 2L)).thenReturn(10L);

        safePaymentService.cleanupExpiredSafePayments();

        verify(paymentEventService).record(100L, 2L, PaymentEventType.EXPIRED_BY_SCHEDULER,
                null, null, null, null);
        verify(tradeService).cancelSafePaymentForBuyerAndAttempt(100L, 2L, ATTEMPT_EXPIRES_AT);
        verify(messageService).saveMessage(any());
        verify(chatMessagePublisher).publishPayment(eq(10L), any());
    }

    @Test
    @DisplayName("만료 건 하나의 정리 실패는 다음 만료 건 처리를 중단하지 않는다")
    void cleanupExpiredSafePayments_oneFailureDoesNotBlockNextTrade() {
        TradeVO first = trade();
        first.setPending_buyer_seq(2L);
        first.setSafe_payment_expire_dtm(ATTEMPT_EXPIRES_AT);
        TradeVO second = trade();
        second.setTrade_seq(101L);
        second.setPending_buyer_seq(3L);
        second.setSafe_payment_expire_dtm(ATTEMPT_EXPIRES_AT.plusSeconds(1));
        when(tradeService.findExpiredPendingSafePayments()).thenReturn(Arrays.asList(first, second));
        doThrow(new RuntimeException("database failure")).when(tradeService)
                .cancelSafePaymentForBuyerAndAttempt(100L, 2L, ATTEMPT_EXPIRES_AT);
        when(tradeService.cancelSafePaymentForBuyerAndAttempt(
                101L, 3L, ATTEMPT_EXPIRES_AT.plusSeconds(1))).thenReturn(true);

        safePaymentService.cleanupExpiredSafePayments();

        verify(paymentEventService).record(101L, 3L, PaymentEventType.EXPIRED_BY_SCHEDULER,
                null, null, null, null);
    }

    @Test
    @DisplayName("스케줄러가 조회한 만료 시도 뒤에 시작된 새 결제는 해제하지 않는다")
    void cleanupExpiredSafePayments_usesExpiredAttemptIdentity() {
        TradeVO expired = trade();
        expired.setPending_buyer_seq(2L);
        expired.setSafe_payment_expire_dtm(ATTEMPT_EXPIRES_AT);
        when(tradeService.findExpiredPendingSafePayments()).thenReturn(Collections.singletonList(expired));
        when(tradeService.cancelSafePaymentForBuyerAndAttempt(100L, 2L, ATTEMPT_EXPIRES_AT))
                .thenReturn(false);

        safePaymentService.cleanupExpiredSafePayments();

        verify(tradeService).cancelSafePaymentForBuyerAndAttempt(100L, 2L, ATTEMPT_EXPIRES_AT);
        verify(paymentEventService, never()).record(100L, 2L, PaymentEventType.EXPIRED_BY_SCHEDULER,
                null, null, null, null);
    }

    @Test
    @DisplayName("승인 결과를 알 수 없으면 CONFIRMING을 유지하고 복구 대상 로그를 기록한다")
    void confirm_unknownLeavesLockedForReconciliation() {
        TossPaymentResponse unknown = new TossPaymentResponse();
        unknown.setCode("UNKNOWN_ERROR");
        when(tradeService.search(100L)).thenReturn(trade());
        when(tradeService.getPaymentCheckInfo(100L)).thenReturn(pending());
        when(paymentEventService.beginConfirmation(100L, 2L, "pay-key", "order-id", 13000, ATTEMPT_EXPIRES_AT)).thenReturn(true);
        when(tossApiService.confirmPayment("pay-key", "order-id", 13000)).thenReturn(unknown);

        SafePaymentService.Result result = safePaymentService.confirm(request());

        assertThat(result.getCode()).isEqualTo("PAYMENT_010");
        verify(paymentEventService).record(100L, 2L, PaymentEventType.CONFIRM_UNKNOWN,
                "pay-key", "order-id", 13000, unknown);
        verify(tradeService, never()).failConfirmingSafePaymentForBuyer(100L, 2L);
    }

    @Test
    @DisplayName("승인 응답이 진행 중이면 결제를 해제하지 않고 재조회 대상으로 유지한다")
    void confirm_nonTerminalStatusLeavesLockedForReconciliation() {
        TossPaymentResponse inProgress = response("IN_PROGRESS");
        when(tradeService.search(100L)).thenReturn(trade());
        when(tradeService.getPaymentCheckInfo(100L)).thenReturn(pending());
        when(paymentEventService.beginConfirmation(100L, 2L, "pay-key", "order-id", 13000, ATTEMPT_EXPIRES_AT))
                .thenReturn(true);
        when(tossApiService.confirmPayment("pay-key", "order-id", 13000)).thenReturn(inProgress);

        SafePaymentService.Result result = safePaymentService.confirm(request());

        assertThat(result.getCode()).isEqualTo("PAYMENT_010");
        verify(paymentEventService).record(100L, 2L, PaymentEventType.CONFIRM_UNKNOWN,
                "pay-key", "order-id", 13000, inProgress);
        verify(tradeService, never()).failConfirmingSafePaymentForBuyer(100L, 2L);
    }

    @Test
    @DisplayName("이미 처리된 승인 응답은 실패로 해제하지 않고 재조회 대상으로 기록한다")
    void confirm_alreadyProcessedLeavesLockedForReconciliation() {
        TossPaymentResponse alreadyProcessed = new TossPaymentResponse();
        alreadyProcessed.setCode("ALREADY_PROCESSED_PAYMENT");
        when(tradeService.search(100L)).thenReturn(trade());
        when(tradeService.getPaymentCheckInfo(100L)).thenReturn(pending());
        when(paymentEventService.beginConfirmation(100L, 2L, "pay-key", "order-id", 13000, ATTEMPT_EXPIRES_AT)).thenReturn(true);
        when(tossApiService.confirmPayment("pay-key", "order-id", 13000)).thenReturn(alreadyProcessed);

        SafePaymentService.Result result = safePaymentService.confirm(request());

        assertThat(result.getCode()).isEqualTo("PAYMENT_010");
        verify(paymentEventService).record(100L, 2L, PaymentEventType.CONFIRM_UNKNOWN,
                "pay-key", "order-id", 13000, alreadyProcessed);
        verify(tradeService, never()).failConfirmingSafePaymentForBuyer(100L, 2L);
    }

    @Test
    @DisplayName("10분 지난 승인 준비 건이 Toss에 없으면 내부 잠금을 해제한다")
    void reconcileUnknownConfirmations_expiredPaymentNotFoundReleasesState() {
        PaymentEventVO event = unknownEvent();
        event.setCreated_dtm(LocalDateTime.now().minusMinutes(11));
        TossPaymentResponse missing = new TossPaymentResponse();
        missing.setCode("NOT_FOUND_PAYMENT");
        when(paymentEventService.findUnresolvedConfirmUnknownEvents()).thenReturn(Collections.singletonList(event));
        when(tossApiService.getPayment("pay-key")).thenReturn(missing);
        when(tradeService.failConfirmingSafePaymentForBuyer(100L, 2L)).thenReturn(true);

        safePaymentService.reconcileUnknownConfirmations();

        verify(tradeService).failConfirmingSafePaymentForBuyer(100L, 2L);
        verify(paymentEventService).record(100L, 2L, PaymentEventType.RECONCILED_FAILURE,
                "pay-key", "order-id", 13000, missing);
    }

    @Test
    @DisplayName("불명확했던 승인이 DONE이면 조회 후 자동 취소하고 CONFIRMING을 해제한다")
    void reconcileUnknownConfirmations_approvedPaymentIsCanceledAndReleased() {
        PaymentEventVO event = unknownEvent();
        TossPaymentResponse approved = response("DONE");
        when(paymentEventService.findUnresolvedConfirmUnknownEvents()).thenReturn(Collections.singletonList(event));
        when(tossApiService.getPayment("pay-key")).thenReturn(approved);
        when(tradeService.failConfirmingSafePaymentForBuyer(100L, 2L)).thenReturn(true);

        safePaymentService.reconcileUnknownConfirmations();

        verify(tossApiService).cancelPayment("pay-key", "승인 결과 확인 지연으로 자동 취소");
        verify(paymentEventService).record(100L, 2L, PaymentEventType.RECONCILED_CANCEL,
                "pay-key", "order-id", 13000, approved);
    }

    @Test
    @DisplayName("불명확했던 결제가 취소 상태면 내부 CONFIRMING만 해제하고 실패 로그를 기록한다")
    void reconcileUnknownConfirmations_failedPaymentReleasesInternalState() {
        PaymentEventVO event = unknownEvent();
        TossPaymentResponse canceled = response("CANCELED");
        when(paymentEventService.findUnresolvedConfirmUnknownEvents()).thenReturn(Collections.singletonList(event));
        when(tossApiService.getPayment("pay-key")).thenReturn(canceled);
        when(tradeService.failConfirmingSafePaymentForBuyer(100L, 2L)).thenReturn(true);

        safePaymentService.reconcileUnknownConfirmations();

        verify(tossApiService, never()).cancelPayment("pay-key", "승인 결과 확인 지연으로 자동 취소");
        verify(paymentEventService).record(100L, 2L, PaymentEventType.RECONCILED_FAILURE,
                "pay-key", "order-id", 13000, canceled);
    }

    @Test
    @DisplayName("부분 취소처럼 모르는 결제 상태는 자동 해제하지 않고 운영 확인 대상으로 유지한다")
    void reconcileUnknownConfirmations_partialCancellationKeepsConfirmationLocked() {
        PaymentEventVO event = unknownEvent();
        TossPaymentResponse partialCanceled = response("PARTIAL_CANCELED");
        when(paymentEventService.findUnresolvedConfirmUnknownEvents()).thenReturn(Collections.singletonList(event));
        when(tossApiService.getPayment("pay-key")).thenReturn(partialCanceled);

        safePaymentService.reconcileUnknownConfirmations();

        verify(tossApiService, never()).cancelPayment("pay-key", "승인 결과 확인 지연으로 자동 취소");
        verify(tradeService, never()).failConfirmingSafePaymentForBuyer(100L, 2L);
    }

    @Test
    @DisplayName("불명확했던 승인 취소 호출이 실패하면 상태를 유지하고 재확인 대상 로그를 기록한다")
    void reconcileUnknownConfirmations_cancelFailureLeavesStateForRetry() {
        PaymentEventVO event = unknownEvent();
        TossPaymentResponse approved = response("DONE");
        when(paymentEventService.findUnresolvedConfirmUnknownEvents()).thenReturn(Collections.singletonList(event));
        when(tossApiService.getPayment("pay-key")).thenReturn(approved);
        doThrow(new RuntimeException("cancel failed")).when(tossApiService)
                .cancelPayment("pay-key", "승인 결과 확인 지연으로 자동 취소");

        safePaymentService.reconcileUnknownConfirmations();

        verify(paymentEventService).record(100L, 2L, PaymentEventType.CANCEL_FAILED,
                "pay-key", "order-id", 13000, approved);
        verify(tradeService, never()).failConfirmingSafePaymentForBuyer(100L, 2L);
    }

    @Test
    @DisplayName("승인 결제 취소 후 내부 상태 해제 실패는 다음 복구 대상 이벤트를 기록한다")
    void confirm_cancelSucceededButStateReleaseFailed_recordsReconciliationEvent() {
        TossPaymentResponse approved = response("DONE");
        when(tradeService.search(100L)).thenReturn(trade());
        when(tradeService.getPaymentCheckInfo(100L)).thenReturn(pending(), confirming());
        when(paymentEventService.beginConfirmation(100L, 2L, "pay-key", "order-id", 13000, ATTEMPT_EXPIRES_AT)).thenReturn(true);
        when(tossApiService.confirmPayment("pay-key", "order-id", 13000)).thenReturn(approved);
        when(tradeService.failConfirmingSafePaymentForBuyer(100L, 2L)).thenReturn(false);

        SafePaymentService.Result result = safePaymentService.confirm(new SafePaymentService.ConfirmRequest(
                100L, 2L, "pay-key", "order-id", 13000, "invalid", null, null, null,
                ATTEMPT_EXPIRES_AT));

        assertThat(result.getCode()).isEqualTo("PAYMENT_012");
        verify(tossApiService).cancelPayment("pay-key", "배송지 정보 오류로 자동 취소");
        verify(paymentEventService).record(100L, 2L, PaymentEventType.RECONCILE_REQUIRED,
                "pay-key", "order-id", 13000, approved);
    }

    private void givenPendingFailure() {
        when(tradeService.search(100L)).thenReturn(trade());
        when(chatroomService.isBuyerOfTrade(100L, 2L)).thenReturn(true);
        when(tradeService.getPaymentCheckInfo(100L)).thenReturn(pending());
        when(tradeService.cancelSafePaymentForBuyerAndAttempt(100L, 2L, ATTEMPT_EXPIRES_AT)).thenReturn(true);
    }

    private TradeVO trade() {
        TradeVO trade = new TradeVO();
        trade.setTrade_seq(100L);
        trade.setSale_price(10000);
        trade.setDelivery_cost(3000);
        trade.setSale_st(SaleStatus.SALE);
        trade.setSafe_payment_st(SafePaymentStatus.PENDING);
        trade.setSafe_payment_expire_dtm(ATTEMPT_EXPIRES_AT);
        return trade;
    }

    private PaymentVO pending() {
        PaymentVO payment = new PaymentVO();
        payment.setSafe_payment_status(SafePaymentStatus.PENDING.name());
        payment.setPending_buyer_seq(2L);
        payment.setRemaining_seconds(100L);
        payment.setSafe_payment_expire_dtm(ATTEMPT_EXPIRES_AT);
        return payment;
    }

    private PaymentVO confirming() {
        PaymentVO payment = pending();
        payment.setSafe_payment_status(SafePaymentStatus.CONFIRMING.name());
        return payment;
    }

    private TossPaymentResponse response(String status) {
        TossPaymentResponse response = new TossPaymentResponse();
        response.setStatus(status);
        response.setMethod("CARD");
        return response;
    }

    private SafePaymentService.ConfirmRequest request() {
        return new SafePaymentService.ConfirmRequest(
                100L, 2L, "pay-key", "order-id", 13000, "direct", null, null, null,
                ATTEMPT_EXPIRES_AT);
    }

    private PaymentEventVO unknownEvent() {
        PaymentEventVO event = new PaymentEventVO();
        event.setTrade_seq(100L);
        event.setMember_seq(2L);
        event.setPayment_key("pay-key");
        event.setOrder_id("order-id");
        event.setAmount(13000);
        return event;
    }
}
