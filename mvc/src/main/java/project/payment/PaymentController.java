package project.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import project.address.AddressVO;
import project.chat.chatroom.ChatroomService;
import project.member.MemberVO;
import project.trade.ENUM.SafePaymentStatus;
import project.trade.ENUM.SaleStatus;
import project.trade.TradeService;
import project.trade.TradeVO;
import project.util.Const;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Controller
public class PaymentController {

    private static final String FAILURE_TOKEN_SESSION_PREFIX = "paymentFailureToken:";

    @Value("${api.toss.client-key}")
    private String tossClientKey;

    private final PaymentService paymentService;
    private final TradeService tradeService;
    private final ChatroomService chatroomService;
    private final SafePaymentService safePaymentService;

    @GetMapping("/payments")
    public String showPayment(HttpSession session, Long trade_seq, Model model) {
        MemberVO sessionMember = (MemberVO) session.getAttribute(Const.SESSION);
        if (sessionMember == null) {
            return "redirect:/";
        }
        TradeVO trade = tradeService.search(trade_seq);
        if (trade == null || trade.getMember_seller_seq() == sessionMember.getMember_seq()) {
            return "redirect:/";
        }
        PaymentVO payment = tradeService.getPaymentCheckInfo(trade_seq);
        if (payment == null) {
            return "redirect:/";
        }
        if (trade.getSale_st() == SaleStatus.SOLD) {
            model.addAttribute("errorMessage", "이미 판매 완료된 상품입니다.");
            return "payment/fail";
        }
        if (!SafePaymentStatus.PENDING.name().equals(payment.getSafe_payment_status())
                || payment.getPending_buyer_seq() == null
                || !payment.getPending_buyer_seq().equals(sessionMember.getMember_seq())) {
            return "redirect:/";
        }
        String failureAttempt = paymentAttempt(trade);
        if (failureAttempt == null) {
            return "redirect:/";
        }
        List<AddressVO> address = paymentService.findAddress(sessionMember.getMember_seq());
        String failureToken = getOrCreateFailureToken(session, trade_seq, failureAttempt);
        model.addAttribute("remainingSeconds", payment.getRemaining_seconds());
        model.addAttribute("addressList", address);
        model.addAttribute("trade", trade);
        model.addAttribute("failureToken", failureToken);
        model.addAttribute("failureAttempt", failureAttempt);
        model.addAttribute("tossClientKey", tossClientKey);
        return "payment/payform";
    }

    @GetMapping("/payments/success")
    public String success(@RequestParam Long trade_seq,
                          @RequestParam String paymentKey,
                          @RequestParam String orderId,
                          @RequestParam int amount,
                          @RequestParam String addr_type,
                          @RequestParam(required = false) String post_no,
                          @RequestParam(required = false) String addr_h,
                          @RequestParam(required = false) String addr_d,
                          @RequestParam String failure_attempt,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        MemberVO buyer = (MemberVO) session.getAttribute(Const.SESSION);
        Long buyerSeq = buyer == null ? null : buyer.getMember_seq();
        LocalDateTime attemptExpiresAt = parsePaymentAttempt(failure_attempt);
        if (attemptExpiresAt == null) {
            redirectAttributes.addFlashAttribute("errorCode", PaymentErrorCode.PAYMENT_UNAVAILABLE.getCode());
            redirectAttributes.addFlashAttribute("errorMessage", PaymentErrorCode.PAYMENT_UNAVAILABLE.getMessage());
            return "redirect:/payments/result?status=fail";
        }
        SafePaymentService.Result result = safePaymentService.confirm(new SafePaymentService.ConfirmRequest(
                trade_seq, buyerSeq, paymentKey, orderId, amount, addr_type, post_no, addr_h, addr_d,
                attemptExpiresAt));
        session.removeAttribute(failureTokenKey(trade_seq, failure_attempt));
        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("payment", result.getPayment());
            return "redirect:/payments/result?status=success";
        }
        redirectAttributes.addFlashAttribute("errorCode", result.getCode());
        redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
        return "redirect:/payments/result?status=fail";
    }

    @GetMapping("/payments/result")
    public String paymentResult(@RequestParam String status, Model model) {
        return "success".equals(status) ? "payment/success" : "payment/fail";
    }

    @GetMapping("/payments/fail")
    public String fail(@RequestParam(required = false) String code,
                       @RequestParam(required = false) String message,
                       @RequestParam(required = false) Long trade_seq,
                       @RequestParam(defaultValue = "TOSS_FAIL") String reason,
                       @RequestParam(required = false) String failure_token,
                       @RequestParam(required = false) String failure_attempt,
                       Model model, HttpSession session) {
        MemberVO sessionMember = (MemberVO) session.getAttribute(Const.SESSION);
        if (trade_seq != null && sessionMember != null) {
            TradeVO trade;
            try {
                trade = tradeService.search(trade_seq);
            } catch (RuntimeException e) {
                return "redirect:/";
            }
            if (!chatroomService.isBuyerOfTrade(trade_seq, sessionMember.getMember_seq())) {
                return "redirect:/";
            }
            LocalDateTime attemptExpiresAt = parsePaymentAttempt(failure_attempt);
            if (attemptExpiresAt != null
                    && attemptExpiresAt.equals(trade.getSafe_payment_expire_dtm())
                    && matchesFailureToken(session, trade_seq, failure_attempt, failure_token)) {
                // Toss failUrl is a GET redirect, so verify the payment-attempt token first.
                safePaymentService.failPendingPayment(
                        trade_seq, sessionMember.getMember_seq(), eventType(reason), attemptExpiresAt);
                session.removeAttribute(failureTokenKey(trade_seq, failure_attempt));
            }
        }
        model.addAttribute("errorCode", code);
        model.addAttribute("errorMessage", message);
        model.addAttribute("trade_seq", trade_seq);
        model.addAttribute("reason", reason);
        return "payment/fail";
    }

    @PostMapping("/payments/failure")
    @ResponseBody
    public Map<String, Object> failure(@RequestParam Long trade_seq,
                                       @RequestParam(defaultValue = "TOSS_FAIL") String reason,
                                       @RequestParam(required = false) String failure_token,
                                       @RequestParam(required = false) String failure_attempt,
                                       HttpSession session) {
        return paymentFailureResponse(trade_seq, eventType(reason), failure_token, failure_attempt, session);
    }

    @PostMapping("/payments/cancel")
    @ResponseBody
    public Map<String, Object> cancel(@RequestParam Long trade_seq,
                                      @RequestParam(required = false) String failure_token,
                                      @RequestParam(required = false) String failure_attempt,
                                      HttpSession session) {
        return paymentFailureResponse(trade_seq, PaymentEventType.USER_CANCEL,
                failure_token, failure_attempt, session);
    }

    @PostMapping("/payments/timeout")
    @ResponseBody
    public Map<String, Object> timeout(@RequestParam Long trade_seq,
                                       @RequestParam(required = false) String failure_token,
                                       @RequestParam(required = false) String failure_attempt,
                                       HttpSession session) {
        return paymentFailureResponse(trade_seq, PaymentEventType.TIMEOUT,
                failure_token, failure_attempt, session);
    }

    @GetMapping("/payments/remaining-time")
    @ResponseBody
    public Map<String, Object> getRemainingTime(@RequestParam Long trade_seq, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        MemberVO sessionMember = (MemberVO) session.getAttribute(Const.SESSION);
        if (sessionMember == null) {
            result.put("error", true);
            result.put("code", PaymentErrorCode.NOT_AUTHENTICATED.getCode());
            result.put("message", PaymentErrorCode.NOT_AUTHENTICATED.getMessage());
            return result;
        }
        long remainingSeconds = tradeService.getSafePaymentExpireSeconds(trade_seq);
        SafePaymentStatus status = tradeService.getSafePaymentStatus(trade_seq);
        result.put("remainingSeconds", remainingSeconds);
        result.put("status", status != null ? status.name() : null);
        return result;
    }

    private Map<String, Object> paymentFailureResponse(Long tradeSeq, PaymentEventType eventType,
                                                       String failureToken, String failureAttempt,
                                                       HttpSession session) {
        LocalDateTime attemptExpiresAt = parsePaymentAttempt(failureAttempt);
        if (attemptExpiresAt == null
                || !matchesFailureToken(session, tradeSeq, failureAttempt, failureToken)) {
            return paymentFailureRejected();
        }
        MemberVO sessionMember = (MemberVO) session.getAttribute(Const.SESSION);
        Long memberSeq = sessionMember == null ? null : sessionMember.getMember_seq();
        SafePaymentService.Result serviceResult = safePaymentService.failPendingPayment(
                tradeSeq, memberSeq, eventType, attemptExpiresAt);
        if (serviceResult.isSuccess()) {
            session.removeAttribute(failureTokenKey(tradeSeq, failureAttempt));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", serviceResult.isSuccess());
        result.put("code", serviceResult.getCode());
        result.put("message", serviceResult.getMessage());
        return result;
    }

    private Map<String, Object> paymentFailureRejected() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("code", PaymentErrorCode.NOT_PENDING.getCode());
        result.put("message", PaymentErrorCode.NOT_PENDING.getMessage());
        return result;
    }

    private PaymentEventType eventType(String reason) {
        if ("TIMEOUT".equals(reason)) {
            return PaymentEventType.TIMEOUT;
        }
        if ("PAGE_LEAVE".equals(reason)) {
            return PaymentEventType.PAGE_LEAVE;
        }
        if ("USER_CANCEL".equals(reason)) {
            return PaymentEventType.USER_CANCEL;
        }
        return PaymentEventType.TOSS_FAIL;
    }

    private boolean matchesFailureToken(HttpSession session, Long tradeSeq, String failureAttempt,
                                        String failureToken) {
        Object expectedToken = session.getAttribute(failureTokenKey(tradeSeq, failureAttempt));
        return failureToken != null && failureToken.equals(expectedToken);
    }

    private String getOrCreateFailureToken(HttpSession session, Long tradeSeq, String failureAttempt) {
        String key = failureTokenKey(tradeSeq, failureAttempt);
        Object existingToken = session.getAttribute(key);
        if (existingToken instanceof String && !((String) existingToken).trim().isEmpty()) {
            return (String) existingToken;
        }
        String failureToken = UUID.randomUUID().toString();
        session.setAttribute(key, failureToken);
        return failureToken;
    }

    private String paymentAttempt(TradeVO trade) {
        return trade.getSafe_payment_expire_dtm() == null ? null : trade.getSafe_payment_expire_dtm().toString();
    }

    private LocalDateTime parsePaymentAttempt(String failureAttempt) {
        if (failureAttempt == null || failureAttempt.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(failureAttempt);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String failureTokenKey(Long tradeSeq, String failureAttempt) {
        return FAILURE_TOKEN_SESSION_PREFIX + tradeSeq + ":" + failureAttempt;
    }
}
