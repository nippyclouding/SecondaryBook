package project.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.address.AddressService;
import project.bookclub.BookClubService;
import project.bookclub.vo.BookClubVO;
import project.trade.TradeService;
import project.trade.TradeVO;
import project.util.Const;
import project.util.exception.ForbiddenException;
import project.util.logInOut.LoginUtil;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MypageController {

    private static final Set<String> VALID_BANK_CODES = Set.of(
            "004", "020", "088", "081", "003", "011", "023", "032", "045", "064", "090", "089", "092"
    );

    private final TradeService tradeService;
    private final BookClubService bookClubService;
    private final AddressService addressService;
    private final MemberBankAccountService memberBankAccountService;

    @GetMapping("/mypage")
    public String mypage(HttpSession sess, Model model) {
        MemberVO loginSess = (MemberVO) sess.getAttribute(Const.SESSION);
        if (loginSess == null) {
            return LoginUtil.redirectToLogin();
        }

        model.addAttribute("currentTab", "profile");
        return "member/mypage";
    }

    @GetMapping("/mypage/{tab}")
    public String mypageWithTab(@PathVariable String tab,
                                @RequestParam(required = false) String status,
                                HttpSession sess,
                                Model model) {
        MemberVO loginSess = (MemberVO) sess.getAttribute(Const.SESSION);
        if (loginSess == null) {
            return LoginUtil.redirectToLogin();
        }

        model.addAttribute("currentTab", tab);
        model.addAttribute("status", status);
        return "member/mypage";
    }

    @GetMapping("/mypage/tab/{tab}")
    public String loadTab(@PathVariable String tab,
                          @RequestParam(required = false) String status,
                          HttpSession sess,
                          Model model
    ) {
        log.info("=========loadTab 호출됨: tab={}, status={}", tab, status);
        // 로그인 체크
        MemberVO loginSess = (MemberVO) sess.getAttribute(Const.SESSION);
        if(loginSess == null) {
            throw new ForbiddenException("로그인이 필요합니다.");
        }

        Long member_seq = loginSess.getMember_seq();
        // 탭별 초기 데이터 로드 (SSR이 필요한 경우 여기서 처리)
        // 현재 대부분 AJAX로 처리하므로 비워두거나 기본값만 설정
        switch (tab) {
            case "profile" :
                log.info("profile 탭로드");
                break;
            case "purchases" :
                log.info("purchases 탭 로드, status={}", status);
                List<TradeVO> purchaseList = tradeService.getPurchaseTrades(member_seq);
                model.addAttribute("purchaseList", purchaseList);
                break;
            case "sales" :
                log.info("sales 탭 로드, status={}", status);
                String salesStatus = status != null ? status : "all";
                List<TradeVO> salesList = tradeService.getSaleTrades(member_seq, salesStatus);
                model.addAttribute("salesList", salesList);
//                model.addAttribute("selectedStatus", salesStatus); // 셀렉트 박스 삭제 요청
                break;
            case "wishlist" :
                break;
            case "groups" :
                // 내 모임은 groups.jsp에서 AJAX로 로딩하므로 여기선 처리 없음
                break;
            case "addresses" :
                break;
            case "bankaccount" :
                break;
            default:
                // 지원하지 않는 탭은 profile로 처리 (redirect 대신 직접 반환)
                log.info("지원하지 않는 탭: {}, profile로 대체", tab);
                return "member/tabs/profile";
        }
        log.info("반환 JSP: member/tabs/{}", tab);
        return "member/tabs/" + tab;
    }

    // ---------------------------------------------------------
    // AJAX 요청 처리 메서드 (JSP의 $.ajax URL과 매핑)
    // ---------------------------------------------------------
    
    // [AJAX] 내 모임 데이터 조회
    @GetMapping("/profile/bookclub/list")
    @ResponseBody
    public List<BookClubVO> getMyBookClubs(HttpSession sess) {
        MemberVO user = (MemberVO) sess.getAttribute(Const.SESSION);
        if (user == null) return null;

        // BookClubService를 통해 데이터 조회
        return bookClubService.getMyBookClubs(user.getMember_seq());
    }
    // [AJAX] 찜한 상품 목록
    @GetMapping("/profile/wishlist/trade")
    @ResponseBody
    public List<TradeVO> getWishTrades(HttpSession sess) {
        MemberVO user = (MemberVO) sess.getAttribute(Const.SESSION);
        if (user == null) return null;
        return tradeService.getWishTrades(user.getMember_seq());
    }

    // [AJAX] 찜한 모임 목록
    @GetMapping("/profile/wishlist/bookclub")
    @ResponseBody
    public List<BookClubVO> getWishBookClubs(HttpSession sess) {
        MemberVO user = (MemberVO) sess.getAttribute(Const.SESSION);
        if (user == null) return null;
        return bookClubService.getWishBookClubs(user.getMember_seq());
    }

    // [AJAX] 정산 계좌 조회 (계좌번호는 뒤 4자리만 노출)
    @GetMapping("/profile/bankaccount/get")
    @ResponseBody
    public MemberBankAccountVO getBankAccount(HttpSession sess) {
        MemberVO user = (MemberVO) sess.getAttribute(Const.SESSION);
        if (user == null) return null;
        MemberBankAccountVO vo = memberBankAccountService.getByMemberSeq(user.getMember_seq());
        if (vo != null && vo.getBank_account_no() != null) {
            String accountNo = vo.getBank_account_no();
            String masked = accountNo.length() > 4
                    ? "*".repeat(accountNo.length() - 4) + accountNo.substring(accountNo.length() - 4)
                    : "****";
            vo.setBank_account_no(masked);
        }
        return vo;
    }

    // [AJAX] 정산 계좌 등록/수정
    @PostMapping("/profile/bankaccount/save")
    @ResponseBody
    public Map<String, Object> saveBankAccount(
            @RequestParam String bank_code,
            @RequestParam String bank_account_no,
            @RequestParam String account_holder_nm,
            HttpSession sess) {

        Map<String, Object> result = new HashMap<>();
        MemberVO user = (MemberVO) sess.getAttribute(Const.SESSION);
        if (user == null) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return result;
        }

        // 입력값 검증
        String cleanBankCode  = bank_code.trim();
        String cleanAccountNo = bank_account_no.replaceAll("[^0-9]", "");
        String cleanHolderNm  = account_holder_nm.trim();

        if (!VALID_BANK_CODES.contains(cleanBankCode)) {
            result.put("success", false);
            result.put("message", "유효하지 않은 은행 코드입니다.");
            return result;
        }
        if (cleanAccountNo.length() < 7 || cleanAccountNo.length() > 20) {
            result.put("success", false);
            result.put("message", "계좌번호는 7~20자리 숫자여야 합니다.");
            return result;
        }
        if (cleanHolderNm.isEmpty() || cleanHolderNm.length() > 20) {
            result.put("success", false);
            result.put("message", "예금주명은 1~20자여야 합니다.");
            return result;
        }

        try {
            MemberBankAccountVO vo = new MemberBankAccountVO();
            vo.setMember_seq(user.getMember_seq());
            vo.setBank_code(cleanBankCode);
            vo.setBank_account_no(cleanAccountNo);
            vo.setAccount_holder_nm(cleanHolderNm);
            memberBankAccountService.save(vo);
            result.put("success", true);
            result.put("message", "계좌가 저장되었습니다.");
        } catch (Exception e) {
            log.error("계좌 저장 실패: member_seq={}", user.getMember_seq(), e);
            result.put("success", false);
            result.put("message", "저장 중 오류가 발생했습니다.");
        }
        return result;
    }

}