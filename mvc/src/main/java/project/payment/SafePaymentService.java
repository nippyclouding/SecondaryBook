package project.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import project.chat.chatroom.ChatroomService;
import project.chat.message.MessageService;
import project.chat.message.MessageVO;
import project.chat.pubsub.ChatMessagePublisher;
import project.trade.ENUM.SafePaymentStatus;
import project.trade.ENUM.SaleStatus;
import project.trade.TradeService;
import project.trade.TradeVO;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SafePaymentService {

    private final TradeService tradeService;
    private final TossApiService tossApiService;
    private final PaymentEventService paymentEventService;
    private final ChatroomService chatroomService;
    private final MessageService messageService;
    private final ChatMessagePublisher chatMessagePublisher;

    public Result confirm(ConfirmRequest request) {
        TradeVO trade;
        try {
            trade = tradeService.search(request.getTradeSeq());
        } catch (RuntimeException e) {
            return Result.failure(PaymentErrorCode.TRADE_NOT_FOUND);
        }
        if (request.getMemberSeq() == null) {
            return Result.failure(PaymentErrorCode.SESSION_EXPIRED);
        }
        if (request.getAttemptExpiresAt() == null
                || !request.getAttemptExpiresAt().equals(trade.getSafe_payment_expire_dtm())) {
            return Result.failure(PaymentErrorCode.PAYMENT_UNAVAILABLE);
        }

        int serverAmount = trade.getSale_price() + trade.getDelivery_cost();
        if (serverAmount != request.getAmount()) {
            handleRequestPendingFailure(request, PaymentEventType.TOSS_FAIL, request.getAmount());
            return Result.failure(PaymentErrorCode.AMOUNT_MISMATCH);
        }
        if (trade.getSale_st() == SaleStatus.SOLD || trade.getSafe_payment_st() != SafePaymentStatus.PENDING) {
            handleRequestPendingFailure(request, PaymentEventType.TOSS_FAIL, serverAmount);
            return Result.failure(PaymentErrorCode.PAYMENT_UNAVAILABLE);
        }

        PaymentVO pendingPayment = tradeService.getPaymentCheckInfo(request.getTradeSeq());
        if (!isCurrentBuyerInStatus(pendingPayment, request.getMemberSeq(), SafePaymentStatus.PENDING)) {
            handleRequestPendingFailure(request, PaymentEventType.TOSS_FAIL, serverAmount);
            return Result.failure(PaymentErrorCode.PAYMENT_FORBIDDEN);
        }
        if (pendingPayment.getRemaining_seconds() <= 0) {
            handleRequestPendingFailure(request, PaymentEventType.TIMEOUT, serverAmount);
            return Result.failure(PaymentErrorCode.PAYMENT_TIMEOUT);
        }
        try {
            boolean beganConfirmation = paymentEventService.beginConfirmation(
                    request.getTradeSeq(), request.getMemberSeq(), request.getPaymentKey(),
                    request.getOrderId(), serverAmount, request.getAttemptExpiresAt());
            if (!beganConfirmation) {
                return Result.failure(PaymentErrorCode.CONFIRM_IN_PROGRESS);
            }
        } catch (Exception e) {
            log.error("결제 승인 시작 이벤트 로그 저장 실패: trade_seq={}", request.getTradeSeq(), e);
            cancelRequestPendingPayment(request);
            return Result.failure(PaymentErrorCode.EVENT_LOG_FAILURE);
        }

        TossPaymentResponse tossResponse = tossApiService.confirmPayment(
                request.getPaymentKey(), request.getOrderId(), serverAmount);
        if (tossResponse == null || isConfirmResultUnknown(tossResponse.getCode())
                || isNonTerminal(tossResponse.getStatus())) {
            recordSafely(request.getTradeSeq(), request.getMemberSeq(), PaymentEventType.CONFIRM_UNKNOWN,
                    request.getPaymentKey(), request.getOrderId(), serverAmount, tossResponse);
            return Result.failure(PaymentErrorCode.CONFIRM_UNKNOWN);
        }
        if (!"DONE".equals(tossResponse.getStatus())) {
            failConfirmingAttempt(request.getTradeSeq(), request.getMemberSeq(), PaymentEventType.TOSS_FAIL,
                    request.getPaymentKey(), request.getOrderId(), serverAmount, tossResponse, true);
            return Result.failure(PaymentErrorCode.TOSS_CONFIRM_FAILURE, tossResponse.getMessage());
        }

        PaymentVO confirmingPayment = tradeService.getPaymentCheckInfo(request.getTradeSeq());
        if (!isCurrentBuyerInStatus(confirmingPayment, request.getMemberSeq(), SafePaymentStatus.CONFIRMING)) {
            boolean canceled = cancelApprovedPayment(request.getTradeSeq(), request.getMemberSeq(),
                    request.getPaymentKey(), request.getOrderId(), serverAmount, tossResponse,
                    PaymentEventType.TOSS_FAIL, "결제 정보 불일치로 자동 취소");
            return canceled ? Result.failure(PaymentErrorCode.PAYMENT_FORBIDDEN)
                    : Result.failure(PaymentErrorCode.CANCEL_REVIEW_REQUIRED);
        }

        PaymentVO payment = buildPayment(request, serverAmount, tossResponse);
        if (payment == null) {
            boolean canceled = cancelApprovedPayment(request.getTradeSeq(), request.getMemberSeq(),
                    request.getPaymentKey(), request.getOrderId(), serverAmount, tossResponse,
                    PaymentEventType.TOSS_FAIL, "배송지 정보 오류로 자동 취소");
            return canceled ? Result.failure(PaymentErrorCode.INVALID_ADDRESS)
                    : Result.failure(PaymentErrorCode.CANCEL_REVIEW_REQUIRED);
        }
        try {
            tradeService.completePurchaseAndNotify(request.getTradeSeq(), request.getMemberSeq(),
                    payment.getPost_no(), payment.getAddr_h(), payment.getAddr_d());
        } catch (Exception e) {
            log.error("결제 DB 처리 실패, 토스 결제 취소 시도: trade_seq={}", request.getTradeSeq(), e);
            boolean canceled = cancelApprovedPayment(request.getTradeSeq(), request.getMemberSeq(),
                    request.getPaymentKey(), request.getOrderId(), serverAmount, tossResponse,
                    PaymentEventType.TOSS_FAIL, "결제 처리 중 서버 오류로 자동 취소");
            return canceled ? Result.failure(PaymentErrorCode.PROCESSING_FAILURE)
                    : Result.failure(PaymentErrorCode.CANCEL_REVIEW_REQUIRED);
        }
        recordSafely(request.getTradeSeq(), request.getMemberSeq(), PaymentEventType.SUCCESS,
                request.getPaymentKey(), request.getOrderId(), serverAmount, tossResponse);
        return Result.success(payment);
    }

    public Result failPendingPayment(Long tradeSeq, Long memberSeq, PaymentEventType eventType,
                                     LocalDateTime attemptExpiresAt) {
        if (memberSeq == null) {
            return Result.failure(PaymentErrorCode.NOT_AUTHENTICATED);
        }
        try {
            tradeService.search(tradeSeq);
        } catch (RuntimeException e) {
            return Result.failure(PaymentErrorCode.ACTION_FORBIDDEN);
        }
        if (!chatroomService.isBuyerOfTrade(tradeSeq, memberSeq)) {
            return Result.failure(PaymentErrorCode.ACTION_FORBIDDEN);
        }
        if (attemptExpiresAt == null) {
            return Result.failure(PaymentErrorCode.NOT_PENDING);
        }
        boolean canceled = handlePendingFailureForAttempt(tradeSeq, memberSeq, eventType, attemptExpiresAt,
                null, null, null, null);
        if (!canceled) {
            return Result.failure(PaymentErrorCode.NOT_PENDING);
        }
        String message = eventType == PaymentEventType.TIMEOUT ? "결제 시간이 만료되었습니다."
                : eventType == PaymentEventType.TOSS_FAIL ? "결제에 실패했습니다." : "결제가 취소되었습니다.";
        return Result.success(message);
    }

    public void cleanupExpiredSafePayments() {
        List<TradeVO> expiredTrades = tradeService.findExpiredPendingSafePayments();
        for (TradeVO trade : expiredTrades) {
            try {
                Long buyerSeq = trade.getPending_buyer_seq();
                LocalDateTime expiresAt = trade.getSafe_payment_expire_dtm();
                if (buyerSeq != null && expiresAt != null
                        && tradeService.cancelSafePaymentForBuyerAndAttempt(
                        trade.getTrade_seq(), buyerSeq, expiresAt)) {
                    recordSafely(trade.getTrade_seq(), buyerSeq, PaymentEventType.EXPIRED_BY_SCHEDULER,
                            null, null, null, null);
                    sendPaymentFailedMessage(trade.getTrade_seq(), buyerSeq);
                }
            } catch (Exception e) {
                log.error("만료 안전결제 정리 실패: trade_seq={}", trade.getTrade_seq(), e);
            }
        }
    }

    public void reconcileUnknownConfirmations() {
        for (PaymentEventVO event : paymentEventService.findUnresolvedConfirmUnknownEvents()) {
            try {
                TossPaymentResponse payment = tossApiService.getPayment(event.getPayment_key());
                if (payment == null) {
                    continue;
                }
                if (payment.getStatus() == null) {
                    if (canReleaseExpiredMissingPayment(event, payment)
                            && !releaseReconciledFailure(event, payment)) {
                        recordSafely(event.getTrade_seq(), event.getMember_seq(), PaymentEventType.RECONCILE_REQUIRED,
                                event.getPayment_key(), event.getOrder_id(), event.getAmount(), payment);
                    }
                    continue;
                }
                if (isNonTerminal(payment.getStatus())) {
                    continue;
                }
                if ("DONE".equals(payment.getStatus())) {
                    cancelApprovedPayment(event.getTrade_seq(), event.getMember_seq(), event.getPayment_key(),
                            event.getOrder_id(), event.getAmount(), payment, PaymentEventType.RECONCILED_CANCEL,
                            "승인 결과 확인 지연으로 자동 취소");
                    continue;
                }
                if (isCanceledOrFailed(payment.getStatus())) {
                    if (!releaseReconciledFailure(event, payment)) {
                        recordSafely(event.getTrade_seq(), event.getMember_seq(), PaymentEventType.RECONCILE_REQUIRED,
                                event.getPayment_key(), event.getOrder_id(), event.getAmount(), payment);
                    }
                } else {
                    log.warn("자동 복구를 보류하는 Toss 상태: trade_seq={}, status={}",
                            event.getTrade_seq(), payment.getStatus());
                }
            } catch (Exception e) {
                recordSafely(event.getTrade_seq(), event.getMember_seq(), PaymentEventType.RECONCILE_REQUIRED,
                        event.getPayment_key(), event.getOrder_id(), event.getAmount(), null);
                log.error("승인 결과 불명확 결제 자동 복구 실패: trade_seq={}", event.getTrade_seq(), e);
            }
        }
    }

    private boolean releaseReconciledFailure(PaymentEventVO event, TossPaymentResponse payment) {
        try {
            return failConfirmingAttempt(event.getTrade_seq(), event.getMember_seq(),
                    PaymentEventType.RECONCILED_FAILURE, event.getPayment_key(), event.getOrder_id(),
                    event.getAmount(), payment, true);
        } catch (Exception e) {
            log.error("실패 결제 내부 상태 해제 실패: trade_seq={}", event.getTrade_seq(), e);
            return false;
        }
    }

    private PaymentVO buildPayment(ConfirmRequest request, int amount, TossPaymentResponse tossResponse) {
        PaymentVO payment = new PaymentVO();
        payment.setAmount(amount);
        payment.setMethod(tossResponse.getMethod());
        payment.setAddr_type(request.getAddrType());
        if ("manual".equals(request.getAddrType()) || "existing".equals(request.getAddrType())) {
            if (request.getPostNo() == null || request.getAddrH() == null || request.getAddrD() == null
                    || request.getAddrH().trim().isEmpty() || request.getAddrD().trim().isEmpty()
                    || request.getAddrH().length() >= 180 || request.getAddrD().length() >= 180) {
                return null;
            }
            payment.setPost_no(HtmlUtils.htmlEscape(request.getPostNo()));
            payment.setAddr_h(HtmlUtils.htmlEscape(request.getAddrH()));
            payment.setAddr_d(HtmlUtils.htmlEscape(request.getAddrD()));
            return payment;
        }
        if ("direct".equals(request.getAddrType())) {
            payment.setPost_no("직거래/반값택배");
            payment.setAddr_h("직거래/반값택배");
            payment.setAddr_d("직거래/반값택배");
            return payment;
        }
        return null;
    }

    private boolean handleRequestPendingFailure(ConfirmRequest request, PaymentEventType eventType, Integer amount) {
        return handlePendingFailureForAttempt(request.getTradeSeq(), request.getMemberSeq(), eventType,
                request.getAttemptExpiresAt(), request.getPaymentKey(), request.getOrderId(), amount, null);
    }

    private void cancelRequestPendingPayment(ConfirmRequest request) {
        tradeService.cancelSafePaymentForBuyerAndAttempt(
                request.getTradeSeq(), request.getMemberSeq(), request.getAttemptExpiresAt());
    }

    private boolean handlePendingFailureForAttempt(Long tradeSeq, Long memberSeq, PaymentEventType eventType,
                                                   LocalDateTime attemptExpiresAt, String paymentKey,
                                                   String orderId, Integer amount,
                                                   TossPaymentResponse tossResponse) {
        PaymentVO payment = tradeService.getPaymentCheckInfo(tradeSeq);
        if (!isCurrentBuyerInStatus(payment, memberSeq, SafePaymentStatus.PENDING)
                || !attemptExpiresAt.equals(payment.getSafe_payment_expire_dtm())
                || !tradeService.cancelSafePaymentForBuyerAndAttempt(tradeSeq, memberSeq, attemptExpiresAt)) {
            return false;
        }
        recordSafely(tradeSeq, memberSeq, eventType, paymentKey, orderId, amount, tossResponse);
        sendPaymentFailedMessage(tradeSeq, memberSeq);
        return true;
    }

    private boolean failConfirmingAttempt(Long tradeSeq, Long memberSeq, PaymentEventType eventType,
                                          String paymentKey, String orderId, Integer amount,
                                          TossPaymentResponse tossResponse, boolean sendMessage) {
        if (!tradeService.failConfirmingSafePaymentForBuyer(tradeSeq, memberSeq)) {
            return false;
        }
        recordSafely(tradeSeq, memberSeq, eventType, paymentKey, orderId, amount, tossResponse);
        if (sendMessage) {
            sendPaymentFailedMessage(tradeSeq, memberSeq);
        }
        return true;
    }

    private boolean cancelApprovedPayment(Long tradeSeq, Long memberSeq, String paymentKey, String orderId,
                                          Integer amount, TossPaymentResponse tossResponse,
                                          PaymentEventType successEventType, String reason) {
        try {
            tossApiService.cancelPayment(paymentKey, reason);
        } catch (Exception e) {
            recordSafely(tradeSeq, memberSeq, PaymentEventType.CANCEL_FAILED,
                    paymentKey, orderId, amount, tossResponse);
            log.error("토스 결제 취소 실패 - 수동 환불 필요: paymentKey={}, trade_seq={}", paymentKey, tradeSeq, e);
            return false;
        }
        boolean released;
        try {
            released = failConfirmingAttempt(tradeSeq, memberSeq, successEventType,
                    paymentKey, orderId, amount, tossResponse, true);
        } catch (Exception e) {
            recordSafely(tradeSeq, memberSeq, PaymentEventType.RECONCILE_REQUIRED,
                    paymentKey, orderId, amount, tossResponse);
            log.error("토스 결제 취소 후 내부 상태 정리 실패: paymentKey={}, trade_seq={}", paymentKey, tradeSeq, e);
            return false;
        }
        if (!released) {
            recordSafely(tradeSeq, memberSeq, PaymentEventType.RECONCILE_REQUIRED,
                    paymentKey, orderId, amount, tossResponse);
            log.error("토스 결제 취소 후 내부 상태 정리 대상 없음: paymentKey={}, trade_seq={}", paymentKey, tradeSeq);
            return false;
        }
        return true;
    }

    private boolean isNonTerminal(String status) {
        return "READY".equals(status) || "IN_PROGRESS".equals(status) || "WAITING_FOR_DEPOSIT".equals(status);
    }

    private boolean isConfirmResultUnknown(String code) {
        return "UNKNOWN_ERROR".equals(code)
                || "CONFIRM_UNKNOWN".equals(code)
                || "ALREADY_PROCESSED_PAYMENT".equals(code);
    }

    private boolean canReleaseExpiredMissingPayment(PaymentEventVO event, TossPaymentResponse payment) {
        boolean paymentNotFound = "NOT_FOUND_PAYMENT".equals(payment.getCode())
                || "NOT_FOUND_PAYMENT_SESSION".equals(payment.getCode());
        return paymentNotFound && event.getCreated_dtm() != null
                && event.getCreated_dtm().isBefore(LocalDateTime.now().minusMinutes(10));
    }

    private boolean isCanceledOrFailed(String status) {
        return "CANCELED".equals(status) || "ABORTED".equals(status) || "EXPIRED".equals(status);
    }

    private boolean isCurrentBuyerInStatus(PaymentVO payment, Long memberSeq, SafePaymentStatus status) {
        return payment != null && status.name().equals(payment.getSafe_payment_status())
                && memberSeq != null && memberSeq.equals(payment.getPending_buyer_seq());
    }

    private void recordSafely(Long tradeSeq, Long memberSeq, PaymentEventType type, String paymentKey,
                              String orderId, Integer amount, TossPaymentResponse response) {
        try {
            paymentEventService.record(tradeSeq, memberSeq, type, paymentKey, orderId, amount, response);
        } catch (Exception e) {
            log.error("결제 이벤트 로그 저장 실패: trade_seq={}, event_type={}", tradeSeq, type, e);
        }
    }

    private void sendPaymentFailedMessage(Long tradeSeq, Long memberSeq) {
        try {
            Long chatRoomSeq = chatroomService.findChatRoomSeqByTradeAndBuyer(tradeSeq, memberSeq);
            if (chatRoomSeq == null) {
                return;
            }
            MessageVO message = new MessageVO();
            message.setChat_room_seq(chatRoomSeq);
            message.setSender_seq(0L);
            message.setChat_cont("[SAFE_PAYMENT_FAILED]");
            messageService.saveMessage(message);
            chatMessagePublisher.publishPayment(chatRoomSeq, message);
        } catch (Exception e) {
            log.error("결제 실패 메시지 전송 실패: trade_seq={}", tradeSeq, e);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ConfirmRequest {
        private final Long tradeSeq;
        private final Long memberSeq;
        private final String paymentKey;
        private final String orderId;
        private final int amount;
        private final String addrType;
        private final String postNo;
        private final String addrH;
        private final String addrD;
        private final LocalDateTime attemptExpiresAt;

    }

    @Getter
    public static class Result {
        private final boolean success;
        private final PaymentVO payment;
        private final PaymentErrorCode errorCode;
        private final String message;

        private Result(boolean success, PaymentVO payment, PaymentErrorCode errorCode, String message) {
            this.success = success;
            this.payment = payment;
            this.errorCode = errorCode;
            this.message = message;
        }

        public static Result success(PaymentVO payment) {
            return new Result(true, payment, null, null);
        }

        public static Result success(String message) {
            return new Result(true, null, null, message);
        }

        public static Result failure(PaymentErrorCode errorCode) {
            return new Result(false, null, errorCode, errorCode.getMessage());
        }

        public static Result failure(PaymentErrorCode errorCode, String message) {
            return new Result(false, null, errorCode,
                    message == null || message.trim().isEmpty() ? errorCode.getMessage() : message);
        }

        public String getCode() {
            return getErrorCode() == null ? null : getErrorCode().getCode();
        }
    }
}
