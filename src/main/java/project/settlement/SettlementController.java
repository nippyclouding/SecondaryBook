package project.settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import project.member.MemberVO;
import project.trade.TradeService;
import project.util.Const;
import project.util.exception.ClientException;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;
    private final TradeService tradeService;

    /**
     * 판매자 정산 신청 API
     * POST /settlement/request/{trade_seq}
     */
    @PostMapping("/settlement/request/{trade_seq}")
    @ResponseBody
    public Map<String, Object> requestSettlement(
            @PathVariable long trade_seq,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        MemberVO sessionMember = (MemberVO) session.getAttribute(Const.SESSION);
        if (sessionMember == null) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return result;
        }

        try {
            boolean success = settlementService.requestSettlement(trade_seq, sessionMember.getMember_seq());
            result.put("success", success);
            if (success) {
                result.put("message", "정산 신청이 완료되었습니다.");
            } else {
                result.put("message", "정산 신청에 실패했습니다.");
            }
        } catch (ClientException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            log.error("정산 신청 실패: trade_seq={}", trade_seq, e);
            result.put("success", false);
            result.put("message", "정산 신청 중 오류가 발생했습니다.");
        }

        return result;
    }

    /**
     * 정산 상세 내역 조회 API (판매자 마이페이지)
     * GET /settlement/{trade_seq}
     */
    @GetMapping("/settlement/{trade_seq}")
    @ResponseBody
    public Map<String, Object> getSettlementDetail(
            @PathVariable long trade_seq,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        MemberVO sessionMember = (MemberVO) session.getAttribute(Const.SESSION);
        if (sessionMember == null) {
            result.put("success", false);
            return result;
        }

        tradeService.validateSellerOwnership(trade_seq, sessionMember.getMember_seq());

        SettlementVO settlement = settlementService.findByTradeSeq(trade_seq);
        result.put("success", settlement != null);
        result.put("settlement", settlement);
        return result;
    }
}
