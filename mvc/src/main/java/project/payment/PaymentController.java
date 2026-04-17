package project.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import project.address.AddressVO;
import project.chat.chatroom.ChatroomService;
import project.chat.message.MessageService;
import project.chat.message.MessageVO;
import project.member.MemberVO;
import project.trade.ENUM.SafePaymentStatus;
import project.trade.ENUM.SaleStatus;
import project.trade.TradeService;
import project.trade.TradeVO;
import project.chat.pubsub.ChatMessagePublisher;
import project.util.Const;

import javax.servlet.http.HttpSession;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
public class PaymentController {

    private final PaymentService paymentService;
    private final TradeService tradeService;
    private final TossApiService tossApiService;
    private final MessageService messageService;
    private final ChatMessagePublisher chatMessagePublisher;
    private final ChatroomService chatroomService;

    @GetMapping("/payments")
    public String showPayment(HttpSession session, Long trade_seq, Model model) {

        MemberVO sessionMember = (MemberVO) session.getAttribute(Const.SESSION);
        if (sessionMember == null) { return "redirect:/"; }

        TradeVO trade = tradeService.search(trade_seq);
        if (trade == null) { return "redirect:/"; }

        // 검증 : 판매자 본인은 결제 불가
        if (trade.getMember_seller_seq() == sessionMember.getMember_seq()) { return "redirect:/"; }

        // 검증 값 일괄조회
        PaymentVO payment = tradeService.getPaymentCheckInfo(trade_seq);
        if (payment == null) { return "redirect:/"; }

        // 남은 결제 시간
        long remainingSeconds = payment.getRemaining_seconds();
        // 안전결제 상태 확인
        String safePaymentStatus = payment.getSafe_payment_status();
        // 안전결제 대상 구매자인지
        Long pendingBuyerSeq = payment.getPending_buyer_seq();

        // 검증 : 이미 판매 완료된 상품인지 체크
        if (trade.getSale_st() == SaleStatus.SOLD) {
            log.error("이미 판매 완료된 상품: trade_seq={}", trade_seq);
            model.addAttribute("errorMessage", "이미 판매 완료된 상품입니다.");
            return "payment/fail";
        }

        // 검증 : 안전결제 상태 확인 (직접 url 접근 시 체크)
        if (!SafePaymentStatus.PENDING.name().equals(safePaymentStatus)) {
            return "redirect:/";  // 안전결제 요청이 없으면 접근 불가
        }

        // 검증 : 안전결제 대상 구매자인지 확인
        if (pendingBuyerSeq == null || !pendingBuyerSeq.equals(sessionMember.getMember_seq())) {
            return "redirect:/";
        }

        // 정상 로직
        List<AddressVO> address = paymentService.findAddress(sessionMember.getMember_seq()); // 구매자 주소 DB 에서 조회 후 전달

        model.addAttribute("remainingSeconds", remainingSeconds);
        model.addAttribute("addressList", address);
        model.addAttribute("trade", trade);

        return "payment/payform";
    }


    // 안전 결제 성공 시 (Toss에서 GET 리다이렉트 → 처리 후 PRG 패턴으로 리다이렉트)
    @GetMapping("/payments/success")
    public String success(@RequestParam Long trade_seq,
                          @RequestParam String paymentKey,
                          @RequestParam String orderId,
                          @RequestParam int amount,
                          @RequestParam String addr_type,
                          @RequestParam(required = false) String post_no,
                          @RequestParam(required = false) String addr_h,
                          @RequestParam(required = false) String addr_d,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {

        // 검증 - 거래 존재 여부
        TradeVO trade = tradeService.search(trade_seq);
        if (trade == null) {
            log.error("거래를 찾을 수 없음: trade_seq={}", trade_seq);
            redirectAttributes.addFlashAttribute("errorMessage", "거래 정보를 찾을 수 없습니다.");
            return "redirect:/payments/result?status=fail";
        }

        // 서버에서 결제 금액 직접 계산 (클라이언트 amount를 신뢰하지 않음)
        int serverAmount = trade.getSale_price() + trade.getDelivery_cost();

        // 검증 - 클라이언트 금액과 서버 금액 불일치 시 조작 시도로 간주
        if (serverAmount != amount) {
            log.error("결제 금액 조작 의심: trade_seq={}, 클라이언트금액={}, 서버금액={}", trade_seq, amount, serverAmount);
            tradeService.cancelSafePayment(trade_seq);
            redirectAttributes.addFlashAttribute("errorMessage", "결제 금액이 일치하지 않습니다.");
            return "redirect:/payments/result?status=fail";
        }

        // 검증 - 판매 상태 (판매된 제품이거나 pending이 아닌 제품일 경우 실패)
        if (trade.getSale_st() == SaleStatus.SOLD || trade.getSafe_payment_st() != SafePaymentStatus.PENDING) {
            tradeService.cancelSafePayment(trade_seq);
            redirectAttributes.addFlashAttribute("errorMessage", "이미 판매 완료된 상품이거나 결제 요청시간이 만료된 상품입니다.");
            return "redirect:/payments/result?status=fail";
        }

        // 검증 모두 통과 시 토스 결제 승인 API 호출 (서버 계산 금액 사용)
        TossPaymentResponse tossResponse = tossApiService.confirmPayment(paymentKey, orderId, serverAmount);

        // 승인 실패 시
        if (tossResponse == null || !"DONE".equals(tossResponse.getStatus())) {
            tradeService.cancelSafePayment(trade_seq);
            redirectAttributes.addFlashAttribute("errorMessage", tossResponse != null ? tossResponse.getMessage() : "결제 승인 실패");
            return "redirect:/payments/result?status=fail";
        }


        // 승인 성공 시 :
        MemberVO buyer = (MemberVO) session.getAttribute(Const.SESSION);
        if (buyer == null) {
            log.warn("결제 승인 후 세션 누락 - 자동 취소: trade_seq={}", trade_seq);
            tossApiService.cancelPayment(paymentKey, "세션 만료로 자동 취소");
            redirectAttributes.addFlashAttribute("errorMessage", "세션이 만료되었습니다. 결제가 자동 취소되었습니다.");
            return "redirect:/payments/result?status=fail";
        }

        // 검증 - 안전결제 대상 구매자인지 재확인 (IDOR 방지)
        PaymentVO paymentCheck = tradeService.getPaymentCheckInfo(trade_seq);
        if (paymentCheck == null || paymentCheck.getPending_buyer_seq() == null
                || !paymentCheck.getPending_buyer_seq().equals(buyer.getMember_seq())) {
            log.warn("결제 구매자 불일치 또는 결제 정보 없음: trade_seq={}, 세션={}", trade_seq, buyer.getMember_seq());
            tossApiService.cancelPayment(paymentKey, "결제 정보 불일치로 자동 취소");
            redirectAttributes.addFlashAttribute("errorMessage", "결제 권한이 없습니다.");
            return "redirect:/payments/result?status=fail";
        }

        PaymentVO payment = new PaymentVO();
        payment.setTrade_seq(trade_seq);
        payment.setMember_buyer_seq(buyer.getMember_seq());
        payment.setPayment_key(paymentKey);
        payment.setAmount(serverAmount);
        payment.setStatus(tossResponse.getStatus());
        payment.setMethod(tossResponse.getMethod());

        // 배송지 타입에 따라 구분해서 처리
        payment.setAddr_type(addr_type);
        if ("manual".equals(addr_type) || "existing".equals(addr_type)) {
            if (addr_h == null || addr_d == null || post_no == null
                    || addr_h.trim().isEmpty() || addr_d.trim().isEmpty()
                    || addr_h.length() >= 180 || addr_d.length() >= 180) {
                log.warn("배송지 정보 오류 - 자동 취소: trade_seq={}", trade_seq);
                tossApiService.cancelPayment(paymentKey, "배송지 정보 오류로 자동 취소");
                redirectAttributes.addFlashAttribute("errorMessage", "배송지 정보가 올바르지 않습니다. 결제가 자동 취소되었습니다.");
                return "redirect:/payments/result?status=fail";
            }

            payment.setPost_no(HtmlUtils.htmlEscape(post_no));
            payment.setAddr_h(HtmlUtils.htmlEscape(addr_h));
            payment.setAddr_d(HtmlUtils.htmlEscape(addr_d));
        } else if ("direct".equals(addr_type)) {
            payment.setPost_no("직거래/반값택배");
            payment.setAddr_h("직거래/반값택배");
            payment.setAddr_d("직거래/반값택배");
        } else {
            log.warn("잘못된 배송지 타입 - 자동 취소: trade_seq={}, addr_type={}", trade_seq, addr_type);
            tossApiService.cancelPayment(paymentKey, "배송지 타입 오류로 자동 취소");
            redirectAttributes.addFlashAttribute("errorMessage", "배송지 정보가 올바르지 않습니다. 결제가 자동 취소되었습니다.");
            return "redirect:/payments/result?status=fail";
        }

        // 구매 확정 + 결제 완료 메시지 저장을 하나의 트랜잭션으로 처리
        try {
            tradeService.completePurchaseAndNotify(trade_seq, buyer.getMember_seq(), payment.getPost_no(), payment.getAddr_h(), payment.getAddr_d());
        } catch (Exception e) {
            // DB 처리 실패 시 토스에 결제 취소 요청 → 구매자 자동 환불
            log.error("결제 DB 처리 실패, 토스 결제 취소 시도: trade_seq={}", trade_seq, e);
            try {
                tossApiService.cancelPayment(paymentKey, "결제 처리 중 서버 오류로 자동 취소");
                redirectAttributes.addFlashAttribute("errorMessage", "결제 처리 중 오류가 발생하여 자동 취소되었습니다. 다시 시도해주세요.");
            } catch (Exception cancelEx) {
                // 취소도 실패 → 수동 환불 필요, 운영팀 알림
                log.error("토스 결제 취소 실패 - 수동 환불 필요: paymentKey={}, trade_seq={}", paymentKey, trade_seq, cancelEx);
                redirectAttributes.addFlashAttribute("errorMessage", "결제는 완료되었으나 처리 중 오류가 발생했습니다. 고객센터로 문의해 주세요.");
            }
            return "redirect:/payments/result?status=fail";
        }

        // PRG 패턴: paymentKey가 URL에 남지 않도록 리다이렉트
        redirectAttributes.addFlashAttribute("payment", payment);
        return "redirect:/payments/result?status=success";
    }

    // PRG 패턴: 결제 결과 페이지 (paymentKey 등 민감정보가 URL에 노출되지 않음)
    @GetMapping("/payments/result")
    public String paymentResult(@RequestParam String status, Model model) {
        // FlashAttribute로 전달된 payment, errorMessage는 자동으로 model에 포함됨
        if ("success".equals(status)) {
            return "payment/success";
        }
        return "payment/fail";
    }

    @GetMapping("/payments/fail")
    public String fail(@RequestParam(required = false) String code,
                       @RequestParam(required = false) String message,
                       @RequestParam(required = false) Long trade_seq,
                       Model model, HttpSession session) {

        MemberVO sessionMember = (MemberVO) session.getAttribute(Const.SESSION);
        if (trade_seq != null && sessionMember != null) {
            TradeVO trade = tradeService.search(trade_seq);
            // 채팅방 참여자(구매자)인지 확인 (결제 실패 시점에서 member_buyer_seq는 아직 설정되지 않았으므로 채팅방으로 검증)
            if (trade != null && chatroomService.isBuyerOfTrade(trade_seq, sessionMember.getMember_seq())) {
                tradeService.cancelSafePayment(trade_seq); // 1.안전결제 상태를 NONE 으로 변경 (재시도 가능)
                sendPaymentFailedMessage(trade_seq, sessionMember.getMember_seq()); // 2.채팅방에 결제 실패 메시지 전송
                log.info("결제 실패로 안전결제 상태 초기화: trade_seq={}", trade_seq);
            } else {
                return "redirect:/"; // 구매자가 아닌 경우 홈으로 리다이렉트 (url로 거래 접근 방지)
            }
        }

        model.addAttribute("errorCode", code);
        model.addAttribute("errorMessage", message);
        model.addAttribute("trade_seq", trade_seq);

        return "payment/fail";
    }

    // 결제 타임아웃 처리 (프론트에서 5분 경과 시 또는 페이지 이탈 시 호출)
    @PostMapping("/payments/timeout")
    @ResponseBody
    public Map<String, Object> timeout(@RequestParam Long trade_seq, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 검증 : 세션 검증
        MemberVO sessionMember = (MemberVO) session.getAttribute(Const.SESSION);
        if (sessionMember == null) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return result;
        }

        // 검증 : 구매자 seq != 세션 seq일 경우
        TradeVO trade = tradeService.search(trade_seq);
        if (trade == null || !chatroomService.isBuyerOfTrade(trade_seq, sessionMember.getMember_seq())) {
            result.put("success", false);
            result.put("message", "권한이 없습니다.");
            return result;
        }

        // 1. 안전결제 상태를 NONE으로 변경 (재시도 가능)
        tradeService.cancelSafePayment(trade_seq);
        log.info("결제 타임아웃으로 안전결제 상태 초기화: trade_seq={}", trade_seq);

        // 2. 채팅방에 결제 실패 메시지 전송
        sendPaymentFailedMessage(trade_seq, sessionMember.getMember_seq());

        result.put("success", true);
        result.put("message", "결제 시간이 만료되었습니다.");
        return result;
    }


    // 남은 결제 시간 조회 api (채팅방에서 실시간 동기화용)
    @GetMapping("/payments/remaining-time")
    @ResponseBody
    public Map<String, Object> getRemainingTime(@RequestParam Long trade_seq, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 세션 검증 추가
        MemberVO sessionMember = (MemberVO) session.getAttribute(Const.SESSION);
        if (sessionMember == null) {
            result.put("error", true);
            result.put("message", "로그인이 필요합니다.");
            return result;
        }

        long remainingSeconds = tradeService.getSafePaymentExpireSeconds(trade_seq);
        SafePaymentStatus status = tradeService.getSafePaymentStatus(trade_seq);

        result.put("remainingSeconds", remainingSeconds);
        result.put("status", status != null ? status.name() : null);
        return result;
    }

    // 결제 실패 시 채팅방에 메시지 전달
    private void sendPaymentFailedMessage(Long trade_seq, Long member_seq) {
        try {
            Long chat_room_seq = chatroomService.findChatRoomSeqByTradeAndBuyer(trade_seq, member_seq);
            if (chat_room_seq != null) {
                MessageVO failMsg = new MessageVO();
                failMsg.setChat_room_seq(chat_room_seq);
                failMsg.setSender_seq(0L); // 시스템 메시지
                failMsg.setChat_cont("[SAFE_PAYMENT_FAILED]");

                messageService.saveMessage(failMsg);
                chatMessagePublisher.publishPayment(chat_room_seq, failMsg); // Redis Pub/Sub 발행

                log.info("결제 실패 메시지 전송: chat_room_seq={}", chat_room_seq);
            }
        } catch (Exception e) {
            log.error("결제 실패 메시지 전송 실패 : trade_seq = {}", trade_seq, e);
            // 상세 예외 처리 마지막에 하기 + 리팩토링
        }
    }

}
