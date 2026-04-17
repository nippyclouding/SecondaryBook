package project.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import project.bookclub.BookClubService;
import project.bookclub.vo.BookClubVO;
import project.member.MemberService;
import project.util.exception.InvalidRequestException;
import project.util.logInOut.LogoutPendingManager;
import project.util.logInOut.UserType;
import project.member.MemberVO;
import project.payment.TossApiService;
import project.payment.TossPaymentResponse;
import project.settlement.SettlementService;
import project.settlement.SettlementStatus;
import project.settlement.SettlementVO;
import project.trade.TradeService;
import project.trade.TradeVO;
import project.util.paging.PageResult;
import project.util.paging.SearchVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@RequiredArgsConstructor
@Controller
@Validated
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final MemberService memberService;
    private final BookClubService bookClubService;
    private final TradeService tradeService;
    private final LogoutPendingManager logoutPendingManager;
    private final SettlementService settlementService;
    private final TossApiService tossApiService;

    @Value("${admin.login.code1}")
    private String adminCode1;

    @Value("${admin.login.code2}")
    private String adminCode2;

    @Value("${api.toss.client-key}")
    private String tossClientKey;
    // 대시보드 뷰
    @GetMapping("")
    public String dashboard(Model model) {

        try {
            model.addAttribute("memberCount", adminService.countAllMembers());
            model.addAttribute("tradeCount", adminService.countAllTrades());
            model.addAttribute("clubCount", adminService.countAllBookClubs());

            // 2. 목록 (테이블 데이터)
            model.addAttribute("members", adminService.getRecentMembers());
            model.addAttribute("trades", adminService.getRecentTrades());
            model.addAttribute("clubs", adminService.getRecentBookClubs());

            // 3. 로그 데이터
            model.addAttribute("userLogs", adminService.getMemberLoginLogs());
            model.addAttribute("adminLogs", adminService.getAdminLoginLogs());

            // 4. 공지사항 목록 추가
            model.addAttribute("notices", adminService.selectNotices());

            // 5. 안전결제 데이터 추가
            model.addAttribute("safePayList", tradeService.findAllSafePays());
            return "admin/dashboard";
        } catch (Exception e) {
            log.error("대시보드 로드 실패", e);
            throw new project.util.exception.ServerException("대시보드 로드에 실패했습니다.", e);
        }
    }

    // [API] 차트 데이터 (NEW)
    @GetMapping("/api/stats")
    @ResponseBody
    public Map<String, Object> getStats() {
        return adminService.getChartData();
    }

    // [API] 회원 목록
    @GetMapping("/api/users")
    @ResponseBody
    public PageResult<MemberVO> getUsers(SearchVO searchVO) {
        List<MemberVO> list = adminService.searchMembers(searchVO);

        int total = adminService.countAllMembersBySearch(searchVO);
        return new PageResult<>(list, total, searchVO.getPage(), searchVO.getSize());
    }

    // [API] 회원 액션 (BAN / ACTIVE / DELETE)
    @PatchMapping("/api/users")
    @ResponseBody
    public String updateUserStatus(@RequestBody Map<String, Object> body) {
        try {
            Long seq = Long.valueOf(body.get("seq").toString());
            String action = (String) body.get("action");

            if ("DELETE".equals(action)) {
                // 회원 자진탈퇴와 동일한 로직 (Trade 삭제 + BookClub 탈퇴 + 회원 삭제)
                memberService.deleteMember(seq);
                // 현재 로그인 중인 경우 즉시 강제 로그아웃
                logoutPendingManager.addForceLogout(UserType.MEMBER, seq);
            } else {
                adminService.handleMemberAction(seq, action);
                if ("BAN".equals(action)) {
                    // BAN 처리 후 현재 세션 즉시 강제 만료
                    logoutPendingManager.addForceLogout(UserType.MEMBER, seq);
                }
            }
            return "ok";
        } catch (InvalidRequestException e) {
            // 활성 결제 존재 등 비즈니스 규칙 위반 (삭제 불가)
            log.warn("회원 액션 거부: {}", e.getMessage());
            return "error:" + e.getMessage();
        } catch (Exception e) {
            log.error("회원 상태 변경 오류", e);
            return "error";
        }
    }

    // [API] 상품 목록
    @GetMapping("/api/trades")
    @ResponseBody
    public PageResult<TradeVO> getTrades(SearchVO searchVO) {
        List<TradeVO> list = adminService.searchTrades(searchVO);

        int total = adminService.countAllTradesBySearch(searchVO);
        return new PageResult<>(list, total, searchVO.getPage(), searchVO.getSize());
    }

    // [API] 상품 액션
    @PatchMapping("/api/trades")
    @ResponseBody
    public String updateTradeStatus(@RequestBody Map<String, Object> body) {
        try {
            Long seq = Long.valueOf(body.get("seq").toString());
            String action = (String) body.get("action");
            adminService.handleTradeAction(seq, action);
            return "ok";
        } catch (Exception e) {
            log.error("상품 상태 변경 오류", e);
            return "error";
        }
    }

    // [API] 안전결제 내역
    @GetMapping("/api/safepaylist")
    @ResponseBody
    public PageResult<TradeVO> getSafePayList(SearchVO searchVO) {
        List<TradeVO> list = adminService.searchSafePayList(searchVO);

        int total = adminService.countAllSafePayListBySearch(searchVO);
        return new PageResult<>(list, total, searchVO.getPage(), searchVO.getSize());
    }

    // [API] 모임 목록
    @GetMapping("/api/clubs")
    @ResponseBody
    public PageResult<BookClubVO> getClubs(SearchVO searchVO) {
        List<BookClubVO> list = adminService.searchBookClubs(searchVO);

        int total = adminService.countAllGroupsBySearch(searchVO);

        return new PageResult<>(list, total, searchVO.getPage(), searchVO.getSize());
    }

    // [API] 모임 삭제
    @PatchMapping("/api/clubs")
    @ResponseBody
    public String updateClubStatus(@RequestBody Map<String, Object> body) {
        Long seq = Long.valueOf(body.get("seq").toString());
        String action = (String) body.get("action");
        if ("DELETE".equals(action)) adminService.deleteBookClub(seq);
        return "ok";
    }

    // 2-a. 접근 코드 입력 페이지 (GET)
    @GetMapping("/access")
    public String accessPage() {
        return "admin/adminAccess";
    }

    // 2-b. 접근 코드 검증 (POST) → 성공 시 세션 플래그 설정 후 로그인 페이지로 이동
    @PostMapping("/access")
    public String verifyAccess(@RequestParam String code1,
                               @RequestParam String code2,
                               HttpSession session,
                               HttpServletRequest request) {
        if (adminCode1.equals(code1) && adminCode2.equals(code2)) {
            session.invalidate();
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("adminAccessGranted", Boolean.TRUE);
            return "redirect:/admin/login";
        }
        return "redirect:/admin/access?error=true";
    }

    // 2-c. 로그인 페이지 이동 (세션 플래그 확인)
    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("adminAccessGranted"))) {
            return "redirect:/admin/access";
        }
        return "admin/adminLogin";
    }

    // 3. 로그인 처리
    @PostMapping("/loginProcess")
    public String loginProcess(@RequestParam String id,
                               @RequestParam String pwd,
                               HttpSession sess,
                               HttpServletRequest request) {
        if (id == null || id.isBlank() || pwd == null || pwd.isBlank()) {
            return "redirect:/admin/login?error=true";
        }
        AdminVO admin = adminService.login(id, pwd);

        if (admin != null) {
            logoutPendingManager.removeForceLogout(UserType.ADMIN, admin.getAdmin_seq());

            sess.invalidate();
            HttpSession newSess = request.getSession(true);
            newSess.setAttribute("adminSess", admin);
            newSess.setMaxInactiveInterval(30 * 60); // 30분

            // 로그인 기록 추가
            String loginIp = getClientIP(request);
            adminService.recordAdminLogin(admin.getAdmin_seq(), loginIp);
            return "redirect:/admin";
        } else {
            return "redirect:/admin/login?error=true";
        }
    }


    // 4. 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession sess, HttpServletRequest request) {
        try {
            AdminVO admin = (AdminVO) sess.getAttribute("adminSess");
            if (admin != null) {
                String logoutIp = getClientIP(request);
                adminService.recordAdminLogout(admin.getAdmin_seq(), logoutIp);
            }
        } catch (Exception e) {
            log.error("관리자 로그아웃 기록 실패", e);
        }
        try {
            sess.invalidate();
        } catch (Exception e) {
            log.warn("세션 무효화 실패", e);
        }
        return "redirect:/";
    }

    @PostMapping("/logout-beacon")
    @ResponseBody
    public void logoutBeacon(HttpSession sess, HttpServletRequest request) {
        try {
            AdminVO adminVO = (AdminVO) sess.getAttribute("adminSess");
            if (adminVO != null) {
                log.info("비콘 수신: 관리자 {} 종료 시도", adminVO.getAdmin_login_id());
                String logoutIp = getClientIP(request);
                adminService.recordAdminLogout(adminVO.getAdmin_seq(), logoutIp);
            }
        } catch (Exception e) {
            log.error("비콘 로그아웃 기록 실패", e);
        }
        try {
            sess.invalidate();
        } catch (Exception e) {
            log.warn("비콘 세션 무효화 실패", e);
        }
        log.info("비콘 처리: 세션 무효화 완료");
    }

    //관리자 로그인 로그 목록
    @GetMapping("/api/adminLogs")
    @ResponseBody
    public PageResult<LoginInfoVO> getAdminLogs(SearchVO searchVO) {
        List<LoginInfoVO> list = adminService.searchAdminLoginLogs(searchVO);

        int total = adminService.countAdminLoginLogsBySearch(searchVO);

        return new PageResult<>(list, total, searchVO.getPage(), searchVO.getSize());
    }

    //사용자 로그인 로그목록
    @GetMapping("/api/userLogs")
    @ResponseBody
    public PageResult<LoginInfoVO> getUserLogs(SearchVO searchVO) {
        List<LoginInfoVO> list = adminService.searchUsersLoginLogs(searchVO);

        int total = adminService.countUsersLoginLogsBySearch(searchVO);

        return new PageResult<>(list, total, searchVO.getPage(), searchVO.getSize());
    }
    // 공지사항 폼 이동
    @GetMapping("notice/create")
    public String noticeWriteForm(HttpSession sess, Model model) {

        AdminVO admin = (AdminVO) sess.getAttribute("adminSess");
        model.addAttribute("adminName", admin.getAdmin_login_id());

        return "admin/noticeWriteForm";
    }

    // IP 추출 메서드
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if (ip != null && ip.indexOf(",") > 0) {
            ip = ip.substring(0, ip.indexOf(","));
        }

        if (ip != null) {
            ip = ip.trim();
            if (!ip.matches("^[0-9a-fA-F.:]+$")) {
                ip = "0.0.0.0";
            }
        }
        return ip;
    }

    // [API] 배너 목록 조회
    @GetMapping("/api/banners")
    @ResponseBody
    public List<BannerVO> getBanners() {
        return adminService.getBanners();
    }

    // [API] 배너 저장 (추가/수정)
    @PostMapping("/api/banners")
    @ResponseBody
    public String saveBanner(@RequestBody BannerVO banner) {
        adminService.saveBanner(banner);
        return "ok";
    }

    // [API] 배너 삭제
    @DeleteMapping("/api/banners/{seq}")
    @ResponseBody
    public String deleteBanner(@PathVariable Long seq) {
        adminService.deleteBanner(seq);
        return "ok";
    }

    // [API] 임시 페이지 저장
    @PostMapping("/api/pages")
    @ResponseBody
    public Long saveTempPage(@RequestBody Map<String, String> body) {
        String title = body.get("title");
        String content = body.get("content");
        return adminService.saveTempPage(title, content);
    }

    // 공지사항 등록
    @PostMapping("/notices")
    @ResponseBody
    public Map<String, Object> creatNotice(
                                           @NotBlank(message = "공지사항 제목을 입력해주세요.")
                                           @Size(max = 200, message = "제목은 200자 이내로 입력해주세요.")
                                           @RequestParam String notice_title,
                                           @RequestParam(required = false) String is_important,
                                           @RequestParam String active,
                                           @NotBlank(message = "공지사항 내용을 입력해주세요.")
                                           @RequestParam String notice_cont,
                                           HttpSession sess
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            AdminVO adminVO = (AdminVO) sess.getAttribute("adminSess");

            NoticeVO noticeVO = new NoticeVO();
            noticeVO.setAdmin_seq(adminVO.getAdmin_seq());
            noticeVO.setNotice_title(notice_title);
            noticeVO.setNotice_cont(notice_cont);

            int priority = "true".equals(is_important) ? 1 : 0;
            noticeVO.setNotice_priority(priority);

            noticeVO.setActive("true".equals(active));

            adminService.insertNotice(noticeVO);

            response.put("success", true);
            response.put("message", "공지사항이 등록되었습니다.");
        } catch (Exception e) {
            log.error("공지사항 등록 실패", e);
            response.put("success", false);
            response.put("message", "등록 중 오류가 발생했습니다.");
        }
        return response;
    }

    // 공지사항 목록 조회
    @GetMapping("/api/notices")
    @ResponseBody
    public PageResult<NoticeVO> getNotices(SearchVO searchVO) {
        List<NoticeVO> list = adminService.searchNotices(searchVO);

        int total = adminService.countAllNoticesBySearch(searchVO);
        return new PageResult<>(list, total, searchVO.getPage(), searchVO.getSize());
    }

    @GetMapping("/notices/view")
    public String viewNotice(@RequestParam Long notice_seq, Model model) {
        adminService.increaseViewCount(notice_seq);

        NoticeVO noticeVO = adminService.selectNotice(notice_seq);
        model.addAttribute("notice", noticeVO);

        return "admin/tabs/noticeView";
    }

    @DeleteMapping("/notices/delete/{notice_seq}")
    @ResponseBody
    public Map<String, Object> deleteNotice(@PathVariable Long notice_seq) {
        Map<String, Object> response = new HashMap<>();

        try {
            adminService.deleteNotice(notice_seq);
            response.put("success", true);
        } catch (Exception e) {
            log.error("공지사항 삭제 실패", e);
            response.put("success", false);
            response.put("message", "삭제 중 오류가 발생했습니다.");
        }

        return response;
    }

    @GetMapping("/notices/edit")
    public String noticeEditForm(@RequestParam Long notice_seq, Model model, HttpSession sess) {

        NoticeVO noticeVO = adminService.selectNotice(notice_seq);
        model.addAttribute("notice", noticeVO);
        return "admin/tabs/noticeEditForm";
    }

    // 공지사항 수정 API
    @PostMapping("/notices/{notice_seq}")
    @ResponseBody
    public Map<String, Object> updateNotice(
            @PathVariable Long notice_seq,
            @NotBlank(message = "공지사항 제목을 입력해주세요.")
            @Size(max = 200, message = "제목은 200자 이내로 입력해주세요.")
            @RequestParam String notice_title,
            @RequestParam(required = false) String is_important,
            @RequestParam String active,
            @NotBlank(message = "공지사항 내용을 입력해주세요.")
            @RequestParam String notice_cont
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            NoticeVO noticeVO = new NoticeVO();
            noticeVO.setNotice_seq(notice_seq);
            noticeVO.setNotice_title(notice_title);
            noticeVO.setNotice_cont(notice_cont);
            noticeVO.setNotice_priority("true".equals(is_important) ? 1 : 0);
            noticeVO.setActive("true".equals(active));

            adminService.updateNotice(noticeVO);

            response.put("success", true);
            response.put("message", "공지사항이 수정되었습니다.");

        } catch (Exception e) {
            response.put("success", false);
            log.error("공지사항 수정 실패", e);
            response.put("message", "수정 중 오류가 발생했습니다.");
        }

        return response;
    }

    @GetMapping("/bookclubs/{id}")
    public String viewBookClubAsAdmin(@PathVariable Long id,
                                      Model model) {
        // 기존 BookClubService 메서드 활용
        BookClubVO bookClub = bookClubService.getBookClubById(id);
        // 필요한 추가 데이터 (멤버 수, 찜 수 등)

        model.addAttribute("bookClub", bookClub);
        model.addAttribute("isAdminView", true);

        return "bookclub/bookclub_detail";  // 기존 JSP 재사용
    }

    // =============================================
    // 관리자 잔액 충전 (Toss 결제)
    // =============================================

    /** 충전 폼 페이지 */
    @GetMapping("/balance/charge")
    public String balanceChargePage(Model model) {
        model.addAttribute("currentBalance", settlementService.getAdminBalance());
        model.addAttribute("tossClientKey", tossClientKey);
        return "admin/balanceCharge";
    }

    /** Toss 결제 성공 콜백 → 승인 + DB 잔액 증가 */
    @GetMapping("/balance/success")
    public String balanceChargeSuccess(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam int amount,
            RedirectAttributes redirectAttributes) {

        TossPaymentResponse tossResponse = tossApiService.confirmPayment(paymentKey, orderId, amount);

        if (tossResponse == null || !"DONE".equals(tossResponse.getStatus())) {
            String msg = tossResponse != null ? tossResponse.getMessage() : "결제 승인 실패";
            log.error("관리자 잔액 충전 Toss 승인 실패: orderId={}, message={}", orderId, msg);
            redirectAttributes.addFlashAttribute("chargeError", msg);
            return "redirect:/admin/balance/charge";
        }

        try {
            settlementService.chargeAdminBalance(paymentKey, orderId, amount);
            redirectAttributes.addFlashAttribute("chargeSuccess",
                    String.format("%,d원이 충전되었습니다.", amount));
            return "redirect:/admin/balance/charge";
        } catch (Exception e) {
            log.error("관리자 잔액 충전 DB 처리 실패, Toss 결제 취소 시도: orderId={}", orderId, e);
            try {
                tossApiService.cancelPayment(paymentKey, "DB 처리 실패로 자동 취소");
                redirectAttributes.addFlashAttribute("chargeError", "충전 처리 중 오류가 발생하여 자동 취소되었습니다.");
            } catch (Exception cancelEx) {
                log.error("Toss 취소 실패 — 수동 처리 필요: paymentKey={}, orderId={}, amount={}원", paymentKey, orderId, amount, cancelEx);
                redirectAttributes.addFlashAttribute("chargeError",
                        "결제 오류가 발생했습니다. 관리자에게 문의하세요. (주문번호: " + orderId + ")");
            }
            return "redirect:/admin/balance/charge";
        }
    }

    /** Toss 결제 실패 콜백 */
    @GetMapping("/balance/fail")
    public String balanceChargeFail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            RedirectAttributes redirectAttributes) {
        log.warn("관리자 잔액 충전 결제 실패: code={}, message={}", code, message);
        redirectAttributes.addFlashAttribute("chargeError",
                message != null ? message : "결제가 취소되었습니다.");
        return "redirect:/admin/balance/charge";
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public Map<String, Object> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(cv -> cv.getMessage())
                .findFirst()
                .orElse("입력값을 확인해주세요.");
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }

    @GetMapping("/trade/{id}")
    public String viewTradeAsAdmin(@PathVariable Long id,
                                   Model model) {
        // 기존 TradeService 메서드 활용
        TradeVO trade = tradeService.search(id);

        model.addAttribute("trade", trade);
        model.addAttribute("isAdminView", true);

        return "trade/tradedetail";  // 기존 JSP 재사용
    }

    // 정산 신청 목록 (REQUESTED)
    @GetMapping("/api/settlement/requested")
    @ResponseBody
    public Map<String, Object> getRequestedSettlements() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("list", settlementService.findByStatus(SettlementStatus.REQUESTED));
        result.put("count", settlementService.countByStatus(SettlementStatus.REQUESTED));
        result.put("adminBalance", settlementService.getAdminBalance());
        return result;
    }

    // 정산 완료 목록 (COMPLETED)
    @GetMapping("/api/settlement/completed")
    @ResponseBody
    public Map<String, Object> getCompletedSettlements() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("list", settlementService.findByStatus(SettlementStatus.COMPLETED));
        result.put("count", settlementService.countByStatus(SettlementStatus.COMPLETED));
        return result;
    }

    /**
     * 이체 완료 확인 (관리자가 은행 앱에서 이체 완료 후 클릭)
     * transfer_confirmed_yn = 1 로 업데이트
     */
    @PostMapping("/api/settlement/confirm-transfer/{settlement_seq}")
    @ResponseBody
    public Map<String, Object> confirmTransfer(@PathVariable long settlement_seq) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = settlementService.confirmTransfer(settlement_seq);
            result.put("success", success);
            result.put("message", success ? "이체 완료 확인되었습니다." : "이미 확인되었거나 처리할 수 없습니다.");
        } catch (Exception e) {
            log.error("이체 완료 확인 실패: settlement_seq={}", settlement_seq, e);
            result.put("success", false);
            result.put("message", "처리 중 오류가 발생했습니다.");
        }
        return result;
    }
}