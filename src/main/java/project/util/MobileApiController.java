package project.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import project.admin.AdminService;
import project.bookclub.BookClubService;
import project.bookclub.dto.BookClubPageResponseDTO;
import project.chat.chatroom.ChatroomService;
import project.chat.chatroom.ChatroomVO;
import project.chat.message.MessageService;
import project.chat.message.MessageVO;
import project.chat.pubsub.ChatMessagePublisher;
import project.member.MemberService;
import project.member.MemberVO;
import project.trade.ENUM.BookStatus;
import project.trade.ENUM.PaymentType;
import project.trade.ENUM.SaleStatus;
import project.trade.TradeService;
import project.trade.TradeVO;
import project.util.book.BookApiService;
import project.util.book.BookVO;
import project.util.imgUpload.ImgService;
import project.util.logInOut.LogoutPendingManager;
import project.util.logInOut.UserType;
import project.util.paging.PageResult;
import org.springframework.web.multipart.MultipartFile;
import project.bookclub.ENUM.JoinRequestResult;
import project.bookclub.vo.BookClubVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 앱 전용 REST API 컨트롤러
 * 모든 경로는 /api/mobile/** 로 시작하며 CSRF 예외 처리됨 (SecurityConfig 참조)
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class MobileApiController {

    private final MemberService memberService;
    private final AdminService adminService;
    private final LogoutPendingManager logoutPendingManager;
    private final TradeService tradeService;
    private final BookClubService bookClubService;
    private final ChatroomService chatroomService;
    private final MessageService messageService;
    private final ChatMessagePublisher chatMessagePublisher;
    private final BookApiService bookApiService;
    private final ImgService imgService;

    // ─── 인증 ─────────────────────────────────────────────────────────────────

    /**
     * POST /api/mobile/login
     * 앱 전용 로그인 — redirect 대신 JSON + JSESSIONID 쿠키 발급
     */
    @PostMapping("/api/mobile/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileLogin(
            @RequestBody Map<String, String> body,
            HttpSession sess,
            HttpServletRequest request) {

        MemberVO vo = new MemberVO();
        vo.setLogin_id(body.get("login_id"));
        vo.setMember_pwd(body.get("member_pwd"));

        MemberVO memberVO = memberService.login(vo);

        if (memberVO == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "아이디 또는 비밀번호가 틀렸습니다.");
            return ResponseEntity.status(401).body(error);
        }

        sess.removeAttribute("adminSess");
        sess.setAttribute(Const.SESSION, memberVO);
        logoutPendingManager.removeForceLogout(UserType.MEMBER, memberVO.getMember_seq());
        memberService.loginLogUpdate(memberVO.getMember_seq());

        String loginIp = getClientIP(request);
        adminService.recordMemberLogin(memberVO.getMember_seq(), loginIp);

        Map<String, Object> result = new HashMap<>();
        result.put("member_seq", memberVO.getMember_seq());
        result.put("member_nicknm", memberVO.getMember_nicknm());
        result.put("member_email", memberVO.getMember_email());
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/mobile/session-check
     * 세션 유효 여부 확인 — 유효하면 MemberVO(비밀번호 제외) 반환
     */
    @GetMapping("/api/mobile/session-check")
    @ResponseBody
    public ResponseEntity<MemberVO> mobileSessionCheck(HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(member);
    }

    /**
     * POST /api/mobile/auth/logout
     * 앱 전용 로그아웃 — 서버 세션 무효화
     */
    @PostMapping("/api/mobile/auth/logout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileLogout(HttpSession session) {
        session.invalidate();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/mobile/auth/signup
     * 앱 전용 회원가입
     * 요청: { login_id, member_pwd, member_email, member_nicknm, member_tel_no? }
     */
    @PostMapping("/api/mobile/auth/signup")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileSignUp(@RequestBody Map<String, String> body) {
        String loginId = body.get("login_id");
        String pwd     = body.get("member_pwd");
        String email   = body.get("member_email");
        String nicknm  = body.get("member_nicknm");

        if (loginId == null || loginId.isBlank() ||
            pwd    == null || pwd.isBlank()     ||
            email  == null || email.isBlank()   ||
            nicknm == null || nicknm.isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "필수 입력값이 누락되었습니다.");
            return ResponseEntity.badRequest().body(err);
        }

        project.member.MemberVO vo = new project.member.MemberVO();
        vo.setLogin_id(loginId);
        vo.setMember_pwd(pwd);
        vo.setMember_email(email);
        vo.setMember_nicknm(nicknm);
        String tel = body.get("member_tel_no");
        if (tel != null && !tel.isBlank()) vo.setMember_tel_no(tel);

        try {
            boolean success = memberService.signUp(vo);
            Map<String, Object> res = new HashMap<>();
            res.put("success", success);
            if (!success) res.put("message", "회원가입에 실패했습니다.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.status(409).body(err);
        }
    }

    // ─── 거래 ─────────────────────────────────────────────────────────────────

    /**
     * GET /api/mobile/trades
     * 거래 목록 조회 (페이징 + 검색)
     */
    @GetMapping("/api/mobile/trades")
    @ResponseBody
    public PageResult<TradeVO> mobileTradeList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "14") int size,
            @RequestParam(required = false) String search_word,
            @RequestParam(required = false) String sale_st,
            @RequestParam(required = false) String category_nm) {

        TradeVO searchVO = new TradeVO();
        try {
            searchVO.setSale_st(sale_st != null ? SaleStatus.valueOf(sale_st) : SaleStatus.SALE);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid sale_st value '{}', defaulting to SALE", sale_st);
            searchVO.setSale_st(SaleStatus.SALE);
        }
        if (search_word != null && !search_word.isBlank()) searchVO.setSearch_word(search_word);
        if (category_nm != null && !category_nm.isBlank()) searchVO.setCategory_nm(category_nm);

        List<TradeVO> trades = tradeService.searchAllWithPaging(page, size, searchVO);
        int total = tradeService.countAll(searchVO);
        return new PageResult<>(trades, total, page, size);
    }

    /**
     * GET /api/mobile/trades/{tradeSeq}
     * 거래 상세 조회 (이미지 목록 포함)
     */
    @GetMapping("/api/mobile/trades/{tradeSeq}")
    @ResponseBody
    public TradeVO mobileTradeDetail(@PathVariable long tradeSeq) {
        return tradeService.search(tradeSeq);
    }

    /**
     * GET /api/mobile/trades/book?query=
     * 도서 검색 (알라딘 API 위임)
     */
    @GetMapping("/api/mobile/trades/book")
    @ResponseBody
    public List<BookVO> mobileBookSearch(@RequestParam String query) {
        return bookApiService.searchBooks(query);
    }

    /**
     * GET /api/mobile/trades/categories
     * 카테고리 목록 (이름만 반환)
     */
    @GetMapping("/api/mobile/trades/categories")
    @ResponseBody
    public List<String> mobileCategoryList() {
        return tradeService.selectCategory().stream()
                .map(TradeVO::getCategory_nm)
                .collect(Collectors.toList());
    }

    /**
     * POST /api/mobile/trades
     * 판매글 등록 (multipart/form-data)
     * 필수: sale_title, book_title, book_img, sale_cont, sale_price, delivery_cost, category_nm
     */
    @PostMapping("/api/mobile/trades")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileRegisterTrade(
            @RequestParam String sale_title,
            @RequestParam String book_title,
            @RequestParam(required = false) String book_author,
            @RequestParam(required = false) String book_publisher,
            @RequestParam String book_img,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Integer book_org_price,
            @RequestParam String sale_cont,
            @RequestParam Integer sale_price,
            @RequestParam(defaultValue = "0") Integer delivery_cost,
            @RequestParam String category_nm,
            @RequestParam(required = false) String book_st,
            @RequestParam(required = false) String payment_type,
            @RequestParam(required = false) String sale_rg,
            @RequestParam(required = false) List<MultipartFile> uploadFiles,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();

        TradeVO tradeVO = new TradeVO();
        tradeVO.setMember_seller_seq(member.getMember_seq());
        tradeVO.setSale_title(sale_title);
        tradeVO.setBook_title(book_title);
        tradeVO.setBook_author(book_author);
        tradeVO.setBook_publisher(book_publisher);
        tradeVO.setBook_img(book_img);
        tradeVO.setIsbn(isbn);
        tradeVO.setBook_org_price(book_org_price);
        tradeVO.setSale_cont(sale_cont);
        tradeVO.setSale_price(sale_price);
        tradeVO.setDelivery_cost(delivery_cost != null ? delivery_cost : 0);
        tradeVO.setCategory_nm(category_nm);
        tradeVO.setSale_rg(sale_rg);

        if (book_st != null) {
            try { tradeVO.setBook_st(BookStatus.valueOf(book_st)); } catch (IllegalArgumentException ignored) {}
        }
        if (payment_type != null) {
            try { tradeVO.setPayment_type(PaymentType.valueOf(payment_type)); } catch (IllegalArgumentException ignored) {}
        }

        // 이미지 업로드
        if (uploadFiles != null) {
            List<MultipartFile> validFiles = new ArrayList<>();
            for (MultipartFile f : uploadFiles) {
                if (f != null && !f.isEmpty()) validFiles.add(f);
            }
            if (!validFiles.isEmpty()) {
                try {
                    List<String> imgUrls = imgService.storeFiles(validFiles);
                    tradeVO.setImgUrls(imgUrls);
                } catch (Exception e) {
                    log.warn("이미지 업로드 실패: {}", e.getMessage());
                }
            }
        }

        if (tradeService.upload(tradeVO)) {
            Map<String, Object> result = new HashMap<>();
            result.put("trade_seq", tradeVO.getTrade_seq());
            result.put("success", true);
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(500).build();
    }

    /**
     * PUT /api/mobile/trades/{tradeSeq}
     * 판매글 수정 — 판매자 본인만 가능 (multipart/form-data)
     * 이미지: keepImageUrls(유지할 기존 URL 목록) + uploadFiles(새 이미지)
     */
    @PutMapping("/api/mobile/trades/{tradeSeq}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileUpdateTrade(
            @PathVariable long tradeSeq,
            @RequestParam(required = false) String sale_title,
            @RequestParam(required = false) String sale_cont,
            @RequestParam(required = false) Integer sale_price,
            @RequestParam(required = false) Integer delivery_cost,
            @RequestParam(required = false) String category_nm,
            @RequestParam(required = false) String book_st,
            @RequestParam(required = false) String payment_type,
            @RequestParam(required = false) String sale_rg,
            @RequestParam(required = false) List<String> keepImageUrls,
            @RequestParam(required = false) List<MultipartFile> uploadFiles,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();

        try {
            tradeService.validateCanModify(tradeSeq, member.getMember_seq());
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(403).body(error);
        }

        // 기존 거래 조회 — 변경하지 않는 필드 보존
        TradeVO existing = tradeService.search(tradeSeq);
        if (existing == null) return ResponseEntity.status(404).build();

        TradeVO tradeVO = new TradeVO();
        tradeVO.setTrade_seq(tradeSeq);

        // 도서 정보는 기존 값 유지
        tradeVO.setBook_img(existing.getBook_img());
        tradeVO.setBook_title(existing.getBook_title());
        tradeVO.setBook_author(existing.getBook_author());
        tradeVO.setBook_publisher(existing.getBook_publisher());
        tradeVO.setBook_org_price(existing.getBook_org_price());
        tradeVO.setIsbn(existing.getIsbn());

        // 변경 가능한 필드 (null이면 기존값 유지)
        tradeVO.setSale_title(sale_title != null ? sale_title : existing.getSale_title());
        tradeVO.setSale_cont(sale_cont != null ? sale_cont : existing.getSale_cont());
        tradeVO.setSale_price(sale_price != null ? sale_price : existing.getSale_price());
        tradeVO.setDelivery_cost(delivery_cost != null ? delivery_cost : existing.getDelivery_cost());
        tradeVO.setSale_rg(sale_rg);

        // 카테고리: 변경 시 category_seq 조회
        if (category_nm != null && !category_nm.equals(existing.getCategory_nm())) {
            tradeVO.setCategory_nm(category_nm);
            tradeService.selectCategory().stream()
                    .filter(c -> c.getCategory_nm().equals(category_nm))
                    .findFirst()
                    .ifPresent(c -> tradeVO.setCategory_seq(c.getCategory_seq()));
        } else {
            tradeVO.setCategory_nm(existing.getCategory_nm());
            tradeVO.setCategory_seq(existing.getCategory_seq());
        }

        // Enum 필드
        if (book_st != null) {
            try { tradeVO.setBook_st(BookStatus.valueOf(book_st)); } catch (IllegalArgumentException ignored) {
                tradeVO.setBook_st(existing.getBook_st());
            }
        } else {
            tradeVO.setBook_st(existing.getBook_st());
        }
        if (payment_type != null) {
            try { tradeVO.setPayment_type(PaymentType.valueOf(payment_type)); } catch (IllegalArgumentException ignored) {
                tradeVO.setPayment_type(existing.getPayment_type());
            }
        } else {
            tradeVO.setPayment_type(existing.getPayment_type());
        }

        // 이미지 처리
        tradeVO.setKeepImageUrls(keepImageUrls != null ? keepImageUrls : new ArrayList<>());
        if (uploadFiles != null) {
            List<MultipartFile> validFiles = new ArrayList<>();
            for (MultipartFile f : uploadFiles) {
                if (f != null && !f.isEmpty()) validFiles.add(f);
            }
            if (!validFiles.isEmpty()) {
                try {
                    tradeVO.setImgUrls(imgService.storeFiles(validFiles));
                } catch (Exception e) {
                    log.warn("이미지 업로드 실패: {}", e.getMessage());
                }
            }
        }

        if (tradeService.modify(tradeSeq, tradeVO)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(500).build();
    }

    /**
     * PATCH /api/mobile/trades/{tradeSeq}/status
     * 판매 상태 변경 — 판매자 본인만 가능
     * 요청: { "sale_st": "SALE" | "RESERVED" | "SOLD" }
     */
    @PatchMapping("/api/mobile/trades/{tradeSeq}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileUpdateTradeStatus(
            @PathVariable long tradeSeq,
            @RequestBody Map<String, String> body,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();

        String saleStStr = body.get("sale_st");
        if (saleStStr == null || saleStStr.isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "sale_st 값이 필요합니다.");
            return ResponseEntity.badRequest().body(err);
        }

        try {
            tradeService.validateCanModify(tradeSeq, member.getMember_seq());
            SaleStatus newStatus = SaleStatus.valueOf(saleStStr.toUpperCase());
            if (tradeService.updateSaleStatus(tradeSeq, newStatus)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("sale_st", newStatus.name());
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.status(500).build();
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "유효하지 않은 상태값: " + saleStStr);
            return ResponseEntity.badRequest().body(err);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", e.getMessage());
            return ResponseEntity.status(403).body(err);
        }
    }

    /**
     * DELETE /api/mobile/trades/{tradeSeq}
     * 판매글 삭제 — 판매자 본인만 가능
     */
    @DeleteMapping("/api/mobile/trades/{tradeSeq}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileDeleteTrade(
            @PathVariable long tradeSeq,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();

        try {
            tradeService.validateCanDelete(tradeSeq, member.getMember_seq());
            if (tradeService.remove(tradeSeq)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.status(500).build();
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(403).body(error);
        }
    }

    /**
     * POST /api/mobile/trades/{tradeSeq}/confirm
     * 구매 확정 — 구매자 본인만 가능
     */
    @PostMapping("/api/mobile/trades/{tradeSeq}/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileConfirmPurchase(
            @PathVariable long tradeSeq,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();

        boolean success = tradeService.confirmPurchase(tradeSeq, member.getMember_seq());
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/mobile/trades/{tradeSeq}/like
     * 찜하기 토글 — 로그인 필요
     * 응답: { liked: bool, wishCount: int }
     */
    @PostMapping("/api/mobile/trades/{tradeSeq}/like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileTradeLike(
            @PathVariable long tradeSeq,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();

        boolean liked = tradeService.saveLike(tradeSeq, member.getMember_seq());
        int wishCount = tradeService.countLikeAll(tradeSeq);

        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("wishCount", wishCount);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/mobile/trades/{tradeSeq}/wish-status
     * 현재 로그인 회원의 찜 여부 + 찜 수 조회
     * 응답: { liked: bool, wishCount: int }
     */
    @GetMapping("/api/mobile/trades/{tradeSeq}/wish-status")
    @ResponseBody
    public Map<String, Object> mobileWishStatus(
            @PathVariable long tradeSeq,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        boolean liked = member != null && tradeService.isWished(tradeSeq, member.getMember_seq());
        int wishCount = tradeService.countLikeAll(tradeSeq);

        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("wishCount", wishCount);
        return result;
    }

    // ─── 채팅 ─────────────────────────────────────────────────────────────────

    /**
     * POST /api/mobile/chatrooms
     * 판매글에서 채팅하기 — 채팅방 생성 또는 기존 채팅방 반환
     * 요청: { trade_seq, member_seller_seq }
     * 응답: ChatroomVO
     */
    @PostMapping("/api/mobile/chatrooms")
    @ResponseBody
    public ResponseEntity<ChatroomVO> mobileCreateOrFindChatroom(
            @RequestBody Map<String, Long> body,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();

        Long tradeSeq = body.get("trade_seq");
        Long sellerSeq = body.get("member_seller_seq");
        if (tradeSeq == null || sellerSeq == null) return ResponseEntity.badRequest().build();

        // 본인 채팅 방지
        if (sellerSeq == member.getMember_seq()) return ResponseEntity.badRequest().build();

        ChatroomVO chatroom = chatroomService.findOrCreateRoom(sellerSeq, member.getMember_seq(), tradeSeq);
        return ResponseEntity.ok(chatroom);
    }

    /**
     * GET /api/mobile/chatrooms
     * 내 채팅방 목록 (페이징)
     * 응답: { rooms: List<ChatroomVO>, hasMore: bool, totalCount: int }
     */
    @GetMapping("/api/mobile/chatrooms")
    @ResponseBody
    public Map<String, Object> mobileChatroomList(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();
        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) {
            result.put("rooms", Collections.emptyList());
            result.put("hasMore", false);
            result.put("totalCount", 0);
            return result;
        }

        long memberSeq = member.getMember_seq();
        List<ChatroomVO> rooms = chatroomService.searchAllWithPaging(memberSeq, limit, offset, null);
        int totalCount = chatroomService.countAll(memberSeq, null);
        boolean hasMore = (offset + rooms.size()) < totalCount;

        result.put("rooms", rooms);
        result.put("hasMore", hasMore);
        result.put("totalCount", totalCount);
        return result;
    }

    /**
     * GET /api/mobile/chat/messages?chat_room_seq=
     * 채팅방 메시지 전체 조회 (읽음 처리 포함)
     */
    @GetMapping("/api/mobile/chat/messages")
    @ResponseBody
    public ResponseEntity<List<MessageVO>> mobileChatMessages(
            @RequestParam long chat_room_seq,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();

        if (!chatroomService.isMemberOfChatroom(chat_room_seq, member.getMember_seq())) {
            return ResponseEntity.status(403).build();
        }

        List<MessageVO> messages = messageService.getAllMessages(chat_room_seq, member.getMember_seq());
        chatMessagePublisher.publishRead(chat_room_seq, member.getMember_seq());
        return ResponseEntity.ok(messages);
    }

    /**
     * POST /api/mobile/chat/send
     * 메시지 전송 (REST 방식) — DB 저장 + Redis Pub/Sub 브로드캐스트
     * 요청: { chat_room_seq, trade_seq, chat_cont }
     */
    @PostMapping("/api/mobile/chat/send")
    @ResponseBody
    public ResponseEntity<MessageVO> mobileSendMessage(
            @RequestBody Map<String, Object> body,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();

        long chatRoomSeq = ((Number) body.get("chat_room_seq")).longValue();
        long tradeSeq    = ((Number) body.get("trade_seq")).longValue();
        String content   = (String) body.get("chat_cont");

        if (content == null || content.isBlank() || content.length() > 1000) {
            return ResponseEntity.badRequest().build();
        }
        if (!chatroomService.isMemberOfChatroom(chatRoomSeq, member.getMember_seq())) {
            return ResponseEntity.status(403).build();
        }

        MessageVO msg = new MessageVO();
        msg.setChat_room_seq(chatRoomSeq);
        msg.setTrade_seq(tradeSeq);
        msg.setSender_seq(member.getMember_seq());
        msg.setChat_cont(content);
        msg.setMember_seller_nicknm(messageService.findBySellerNicknm(member.getMember_seq()));

        messageService.saveMessage(msg);
        chatroomService.updateLastMessage(chatRoomSeq, content);
        chatMessagePublisher.publishChat(chatRoomSeq, msg);

        return ResponseEntity.ok(msg);
    }

    // ─── 독서모임 ─────────────────────────────────────────────────────────────

    /**
     * GET /api/mobile/bookclubs
     * 독서모임 목록 조회 (페이징 + 키워드 검색)
     */
    @GetMapping("/api/mobile/bookclubs")
    @ResponseBody
    public BookClubPageResponseDTO mobileBookClubList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "latest") String sort,
            HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        Long memberSeq = (member != null) ? member.getMember_seq() : null;
        return bookClubService.searchBookClubs(keyword, sort, page, memberSeq);
    }

    /**
     * GET /api/mobile/bookclubs/{bookClubSeq}
     * 독서모임 상세 조회 (가입 여부, 신청 대기, 찜 여부 포함)
     */
    @GetMapping("/api/mobile/bookclubs/{bookClubSeq}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileBookClubDetail(
            @PathVariable Long bookClubSeq,
            HttpSession session) {

        BookClubVO bookClub = bookClubService.getBookClubById(bookClubSeq);
        if (bookClub == null) return ResponseEntity.status(404).build();

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        Long memberSeq = (member != null) ? member.getMember_seq() : null;

        boolean isJoined          = memberSeq != null && bookClubService.isMemberJoined(bookClubSeq, memberSeq);
        boolean hasPendingRequest  = memberSeq != null && bookClubService.hasPendingRequest(bookClubSeq, memberSeq);
        boolean isWished           = memberSeq != null && bookClubService.isWished(bookClubSeq, memberSeq);
        int wishCount              = bookClubService.getWishCount(bookClubSeq);
        int joinedMemberCount      = bookClubService.getTotalJoinedMemberCount(bookClubSeq);

        Map<String, Object> result = new HashMap<>();
        result.put("bookClub", bookClub);
        result.put("isJoined", isJoined);
        result.put("hasPendingRequest", hasPendingRequest);
        result.put("isWished", isWished);
        result.put("wishCount", wishCount);
        result.put("joinedMemberCount", joinedMemberCount);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/mobile/bookclubs/{bookClubSeq}/join
     * 독서모임 가입 신청 — 로그인 필요
     * 요청: { "request_cont": "..." } (optional)
     */
    @PostMapping("/api/mobile/bookclubs/{bookClubSeq}/join")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileBookClubJoin(
            @PathVariable Long bookClubSeq,
            @RequestBody(required = false) Map<String, String> body,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();

        String requestCont = (body != null) ? body.get("request_cont") : null;
        JoinRequestResult joinResult = bookClubService.createJoinRequest(
                bookClubSeq, member.getMember_seq(), requestCont);

        Map<String, Object> result = new HashMap<>();
        result.put("result", joinResult.name());
        result.put("success", joinResult == JoinRequestResult.SUCCESS);
        if (joinResult != JoinRequestResult.SUCCESS) {
            String message;
            if (joinResult == JoinRequestResult.ALREADY_JOINED) {
                message = "이미 가입된 모임입니다.";
            } else if (joinResult == JoinRequestResult.ALREADY_REQUESTED) {
                message = "이미 가입 신청한 모임입니다.";
            } else {
                message = "가입 신청에 실패했습니다.";
            }
            result.put("message", message);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/mobile/bookclubs/{bookClubSeq}/leave
     * 독서모임 탈퇴 — 로그인 필요
     */
    @PostMapping("/api/mobile/bookclubs/{bookClubSeq}/leave")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileBookClubLeave(
            @PathVariable Long bookClubSeq,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();

        try {
            Map<String, Object> leaveResult = bookClubService.leaveBookClub(bookClubSeq, member.getMember_seq());
            return ResponseEntity.ok(leaveResult);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * POST /api/mobile/bookclubs/{bookClubSeq}/wish
     * 독서모임 찜 토글 — 로그인 필요
     * 응답: { wished: bool, wishCount: int }
     */
    @PostMapping("/api/mobile/bookclubs/{bookClubSeq}/wish")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileBookClubWish(
            @PathVariable Long bookClubSeq,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();

        boolean wished = bookClubService.toggleWish(bookClubSeq, member.getMember_seq());
        int wishCount  = bookClubService.getWishCount(bookClubSeq);

        Map<String, Object> result = new HashMap<>();
        result.put("wished", wished);
        result.put("wishCount", wishCount);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/mobile/bookclubs
     * 독서모임 생성 — 로그인 필요
     * 요청: { book_club_name, book_club_desc, book_club_rg?, book_club_schedule?, book_club_max_member? }
     */
    @PostMapping("/api/mobile/bookclubs")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileCreateBookClub(
            @RequestBody Map<String, Object> body,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();

        String name = (String) body.get("book_club_name");
        String desc = (String) body.get("book_club_desc");
        if (name == null || name.isBlank() || desc == null || desc.isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "모임명과 소개를 입력해주세요.");
            return ResponseEntity.badRequest().body(err);
        }

        BookClubVO vo = new BookClubVO();
        vo.setBook_club_leader_seq(member.getMember_seq());
        vo.setBook_club_name(name.trim());
        vo.setBook_club_desc(desc.trim());
        String rg = (String) body.get("book_club_rg");
        if (rg != null && !rg.isBlank()) vo.setBook_club_rg(rg.trim());
        String schedule = (String) body.get("book_club_schedule");
        if (schedule != null && !schedule.isBlank()) vo.setBook_club_schedule(schedule.trim());
        Object maxMem = body.get("book_club_max_member");
        if (maxMem instanceof Number) vo.setBook_club_max_member(((Number) maxMem).intValue());

        bookClubService.createBookClub(vo);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("book_club_seq", vo.getBook_club_seq());
        return ResponseEntity.ok(result);
    }

    // ─── 독서모임 모임장 관리 ─────────────────────────────────────────────────

    /**
     * GET /api/mobile/bookclubs/{bookClubSeq}/manage
     * 모임장 관리 데이터 — 가입 신청 목록 + 멤버 목록 (모임장만 가능)
     */
    @GetMapping("/api/mobile/bookclubs/{bookClubSeq}/manage")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileBookClubManage(
            @PathVariable Long bookClubSeq,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();
        if (!bookClubService.isLeader(bookClubSeq, member.getMember_seq()))
            return ResponseEntity.status(403).build();

        List<project.bookclub.dto.BookClubJoinRequestDTO> requests =
                bookClubService.getPendingRequestsForManage(bookClubSeq);
        List<project.bookclub.dto.BookClubManageMemberDTO> members =
                bookClubService.getJoinedMembersForManage(bookClubSeq);

        Map<String, Object> result = new HashMap<>();
        result.put("requests", requests);
        result.put("members", members);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/mobile/bookclubs/{bookClubSeq}/manage/requests/{requestSeq}/approve
     * 가입 신청 승인 — 모임장만 가능
     */
    @PostMapping("/api/mobile/bookclubs/{bookClubSeq}/manage/requests/{requestSeq}/approve")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileApproveRequest(
            @PathVariable Long bookClubSeq,
            @PathVariable Long requestSeq,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();
        if (!bookClubService.isLeader(bookClubSeq, member.getMember_seq()))
            return ResponseEntity.status(403).build();

        try {
            bookClubService.approveJoinRequest(bookClubSeq, requestSeq, member.getMember_seq());
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    /**
     * POST /api/mobile/bookclubs/{bookClubSeq}/manage/requests/{requestSeq}/reject
     * 가입 신청 거절 — 모임장만 가능
     */
    @PostMapping("/api/mobile/bookclubs/{bookClubSeq}/manage/requests/{requestSeq}/reject")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileRejectRequest(
            @PathVariable Long bookClubSeq,
            @PathVariable Long requestSeq,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();
        if (!bookClubService.isLeader(bookClubSeq, member.getMember_seq()))
            return ResponseEntity.status(403).build();

        bookClubService.rejectJoinRequest(bookClubSeq, requestSeq, member.getMember_seq());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/mobile/bookclubs/{bookClubSeq}/manage/members/{memberSeq}
     * 멤버 강퇴 — 모임장만 가능
     */
    @DeleteMapping("/api/mobile/bookclubs/{bookClubSeq}/manage/members/{memberSeq}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileKickMember(
            @PathVariable Long bookClubSeq,
            @PathVariable Long memberSeq,
            HttpSession session) {

        MemberVO leader = (MemberVO) session.getAttribute(Const.SESSION);
        if (leader == null) return ResponseEntity.status(401).build();
        if (!bookClubService.isLeader(bookClubSeq, leader.getMember_seq()))
            return ResponseEntity.status(403).build();

        try {
            bookClubService.kickMember(bookClubSeq, leader.getMember_seq(), memberSeq);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    /**
     * PUT /api/mobile/bookclubs/{bookClubSeq}/manage/settings
     * 모임 설정 변경 — 모임장만 가능
     * 요청: { name, description, region?, schedule? }
     */
    @PutMapping("/api/mobile/bookclubs/{bookClubSeq}/manage/settings")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileUpdateBookClubSettings(
            @PathVariable Long bookClubSeq,
            @RequestBody Map<String, Object> body,
            HttpSession session) {

        MemberVO leader = (MemberVO) session.getAttribute(Const.SESSION);
        if (leader == null) return ResponseEntity.status(401).build();
        if (!bookClubService.isLeader(bookClubSeq, leader.getMember_seq()))
            return ResponseEntity.status(403).build();

        project.bookclub.dto.BookClubUpdateSettingsDTO dto =
                new project.bookclub.dto.BookClubUpdateSettingsDTO();
        if (body.get("name")        != null) dto.setName((String) body.get("name"));
        if (body.get("description") != null) dto.setDescription((String) body.get("description"));
        if (body.get("region")      != null) dto.setRegion((String) body.get("region"));
        if (body.get("schedule")    != null) dto.setSchedule((String) body.get("schedule"));

        try {
            Map<String, Object> serviceResult =
                    bookClubService.updateBookClubSettings(bookClubSeq, leader.getMember_seq(), dto);
            serviceResult.put("success", true);
            return ResponseEntity.ok(serviceResult);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    // ─── 독서모임 게시판 ──────────────────────────────────────────────────────

    /**
     * GET /api/mobile/bookclubs/{bookClubSeq}/boards
     * 최근 게시글 목록 (멤버 or 모임장만)
     */
    @GetMapping("/api/mobile/bookclubs/{bookClubSeq}/boards")
    @ResponseBody
    public ResponseEntity<List<project.bookclub.vo.BookClubBoardVO>> mobileGetBoards(
            @PathVariable Long bookClubSeq,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();
        boolean canAccess = bookClubService.isMemberJoined(bookClubSeq, member.getMember_seq())
                || bookClubService.isLeader(bookClubSeq, member.getMember_seq());
        if (!canAccess) return ResponseEntity.status(403).build();

        List<project.bookclub.vo.BookClubBoardVO> boards = bookClubService.getRecentBoards(bookClubSeq);
        return ResponseEntity.ok(boards);
    }

    /**
     * POST /api/mobile/bookclubs/{bookClubSeq}/boards
     * 게시글 작성 (멤버 or 모임장만)
     * 요청: { board_title, board_cont }
     */
    @PostMapping("/api/mobile/bookclubs/{bookClubSeq}/boards")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileCreateBoard(
            @PathVariable Long bookClubSeq,
            @RequestBody Map<String, String> body,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();
        boolean canAccess = bookClubService.isMemberJoined(bookClubSeq, member.getMember_seq())
                || bookClubService.isLeader(bookClubSeq, member.getMember_seq());
        if (!canAccess) return ResponseEntity.status(403).build();

        String title = body.get("board_title");
        String cont  = body.get("board_cont");
        if (title == null || title.isBlank() || cont == null || cont.isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "제목과 내용을 입력해주세요.");
            return ResponseEntity.badRequest().body(err);
        }

        project.bookclub.vo.BookClubBoardVO boardVO = new project.bookclub.vo.BookClubBoardVO();
        boardVO.setBook_club_seq(bookClubSeq);
        boardVO.setMember_seq(member.getMember_seq());
        boardVO.setBoard_title(title.trim());
        boardVO.setBoard_cont(cont.trim());

        Long boardSeq = bookClubService.createBoardPost(boardVO);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("book_club_board_seq", boardSeq);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/mobile/bookclubs/{bookClubSeq}/boards/{boardSeq}
     * 게시글 상세 + 댓글 목록
     */
    @GetMapping("/api/mobile/bookclubs/{bookClubSeq}/boards/{boardSeq}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileGetBoardDetail(
            @PathVariable Long bookClubSeq,
            @PathVariable Long boardSeq,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();
        boolean canAccess = bookClubService.isMemberJoined(bookClubSeq, member.getMember_seq())
                || bookClubService.isLeader(bookClubSeq, member.getMember_seq());
        if (!canAccess) return ResponseEntity.status(403).build();

        project.bookclub.vo.BookClubBoardVO post = bookClubService.getBoardDetail(bookClubSeq, boardSeq);
        if (post == null) return ResponseEntity.status(404).build();

        List<project.bookclub.vo.BookClubBoardVO> comments = bookClubService.getBoardComments(bookClubSeq, boardSeq);

        // 좋아요 정보 설정
        post.setLike_count(bookClubService.getBoardLikeCount(boardSeq));
        post.setIs_liked(bookClubService.isBoardLiked(boardSeq, member.getMember_seq()));

        Map<String, Object> result = new HashMap<>();
        result.put("post", post);
        result.put("comments", comments);
        return ResponseEntity.ok(result);
    }

    /**
     * PUT /api/mobile/bookclubs/{bookClubSeq}/boards/{boardSeq}
     * 게시글 수정 — 작성자 본인만
     * 요청: { board_title, board_cont }
     */
    @PutMapping("/api/mobile/bookclubs/{bookClubSeq}/boards/{boardSeq}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileUpdateBoard(
            @PathVariable Long bookClubSeq,
            @PathVariable Long boardSeq,
            @RequestBody Map<String, String> body,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();

        project.bookclub.vo.BookClubBoardVO existing = bookClubService.getBoardDetail(bookClubSeq, boardSeq);
        if (existing == null) return ResponseEntity.status(404).build();
        if (!existing.getMember_seq().equals(member.getMember_seq()))
            return ResponseEntity.status(403).build();

        String title = body.get("board_title");
        String cont  = body.get("board_cont");
        existing.setBoard_title(title != null && !title.isBlank() ? title.trim() : existing.getBoard_title());
        existing.setBoard_cont(cont != null && !cont.isBlank() ? cont.trim() : existing.getBoard_cont());

        boolean success = bookClubService.updateBoardPost(existing, existing.getBoard_img_url());
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/mobile/bookclubs/{bookClubSeq}/boards/{boardSeq}
     * 게시글 삭제 — 작성자 or 모임장
     */
    @DeleteMapping("/api/mobile/bookclubs/{bookClubSeq}/boards/{boardSeq}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileDeleteBoard(
            @PathVariable Long bookClubSeq,
            @PathVariable Long boardSeq,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();

        project.bookclub.vo.BookClubBoardVO existing = bookClubService.getBoardDetail(bookClubSeq, boardSeq);
        if (existing == null) return ResponseEntity.status(404).build();
        boolean isAuthor = existing.getMember_seq().equals(member.getMember_seq());
        boolean isLeader = bookClubService.isLeader(bookClubSeq, member.getMember_seq());
        if (!isAuthor && !isLeader) return ResponseEntity.status(403).build();

        boolean success = bookClubService.deleteBoardPost(bookClubSeq, boardSeq);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/mobile/bookclubs/{bookClubSeq}/boards/{boardSeq}/comments
     * 댓글 작성
     * 요청: { board_cont }
     */
    @PostMapping("/api/mobile/bookclubs/{bookClubSeq}/boards/{boardSeq}/comments")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileCreateComment(
            @PathVariable Long bookClubSeq,
            @PathVariable Long boardSeq,
            @RequestBody Map<String, String> body,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();
        boolean canAccess = bookClubService.isMemberJoined(bookClubSeq, member.getMember_seq())
                || bookClubService.isLeader(bookClubSeq, member.getMember_seq());
        if (!canAccess) return ResponseEntity.status(403).build();

        String cont = body.get("board_cont");
        if (cont == null || cont.isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "댓글 내용을 입력해주세요.");
            return ResponseEntity.badRequest().body(err);
        }

        int rows = bookClubService.createBoardComment(bookClubSeq, boardSeq, member.getMember_seq(), cont.trim());
        Map<String, Object> result = new HashMap<>();
        result.put("success", rows > 0);
        return ResponseEntity.ok(result);
    }

    /**
     * PUT /api/mobile/bookclubs/{bookClubSeq}/boards/{boardSeq}/comments/{commentId}
     * 댓글 수정 — 작성자 본인만
     * 요청: { board_cont }
     */
    @PutMapping("/api/mobile/bookclubs/{bookClubSeq}/boards/{boardSeq}/comments/{commentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileUpdateComment(
            @PathVariable Long bookClubSeq,
            @PathVariable Long boardSeq,
            @PathVariable Long commentId,
            @RequestBody Map<String, String> body,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();

        project.bookclub.vo.BookClubBoardVO comment = bookClubService.getBoardCommentById(commentId);
        if (comment == null) return ResponseEntity.status(404).build();
        if (!comment.getMember_seq().equals(member.getMember_seq()))
            return ResponseEntity.status(403).build();

        String cont = body.get("board_cont");
        if (cont == null || cont.isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "댓글 내용을 입력해주세요.");
            return ResponseEntity.badRequest().body(err);
        }

        boolean success = bookClubService.updateBoardComment(commentId, cont.trim());
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/mobile/bookclubs/{bookClubSeq}/boards/{boardSeq}/comments/{commentId}
     * 댓글 삭제 — 작성자 or 모임장
     */
    @DeleteMapping("/api/mobile/bookclubs/{bookClubSeq}/boards/{boardSeq}/comments/{commentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileDeleteComment(
            @PathVariable Long bookClubSeq,
            @PathVariable Long boardSeq,
            @PathVariable Long commentId,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();

        project.bookclub.vo.BookClubBoardVO comment = bookClubService.getBoardCommentById(commentId);
        if (comment == null) return ResponseEntity.status(404).build();
        boolean isAuthor = comment.getMember_seq().equals(member.getMember_seq());
        boolean isLeader = bookClubService.isLeader(bookClubSeq, member.getMember_seq());
        if (!isAuthor && !isLeader) return ResponseEntity.status(403).build();

        boolean success = bookClubService.deleteBoardComment(commentId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/mobile/bookclubs/{bookClubSeq}/boards/{boardSeq}/like
     * 게시글 좋아요 토글
     */
    @PostMapping("/api/mobile/bookclubs/{bookClubSeq}/boards/{boardSeq}/like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileToggleBoardLike(
            @PathVariable Long bookClubSeq,
            @PathVariable Long boardSeq,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();

        boolean liked = bookClubService.toggleBoardLike(boardSeq, member.getMember_seq());
        int likeCount = bookClubService.getBoardLikeCount(boardSeq);

        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", likeCount);
        return ResponseEntity.ok(result);
    }

    // ─── 마이페이지 ───────────────────────────────────────────────────────────

    /**
     * GET /api/mobile/mypage/profile
     * 로그인 회원 프로필 정보 반환
     */
    @GetMapping("/api/mobile/mypage/profile")
    @ResponseBody
    public ResponseEntity<MemberVO> mobileMyProfile(HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(member);
    }

    /**
     * GET /api/mobile/mypage/sales
     * 내 판매 목록 조회
     * @param status 판매 상태 필터 (선택, 기본 전체)
     */
    @GetMapping("/api/mobile/mypage/sales")
    @ResponseBody
    public ResponseEntity<List<TradeVO>> mobileMyeSales(
            @RequestParam(required = false) String status,
            HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();
        List<TradeVO> sales = tradeService.getSaleTrades(member.getMember_seq(), status);
        return ResponseEntity.ok(sales);
    }

    /**
     * GET /api/mobile/mypage/purchases
     * 내 구매 목록 조회
     */
    @GetMapping("/api/mobile/mypage/purchases")
    @ResponseBody
    public ResponseEntity<List<TradeVO>> mobileMypurchases(HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();
        List<TradeVO> purchases = tradeService.getPurchaseTrades(member.getMember_seq());
        return ResponseEntity.ok(purchases);
    }

    /**
     * GET /api/mobile/mypage/wishlist
     * 내 찜 목록 조회
     */
    @GetMapping("/api/mobile/mypage/wishlist")
    @ResponseBody
    public ResponseEntity<List<TradeVO>> mobileMyWishlist(HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();
        List<TradeVO> wishlist = tradeService.getWishTrades(member.getMember_seq());
        return ResponseEntity.ok(wishlist);
    }

    /**
     * PUT /api/mobile/mypage/profile
     * 프로필 수정 — 닉네임, 전화번호 변경
     * 요청: { member_nicknm?, member_tel_no? }
     */
    @PutMapping("/api/mobile/mypage/profile")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileUpdateProfile(
            @RequestBody Map<String, String> body,
            HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();

        String nicknm = body.get("member_nicknm");
        String tel    = body.get("member_tel_no");

        if (nicknm != null && !nicknm.isBlank()) {
            member.setMember_nicknm(nicknm);
        }
        if (tel != null) {
            member.setMember_tel_no(tel.isBlank() ? null : tel);
        }

        boolean success = memberService.updateMember(member);
        if (success) {
            session.setAttribute(Const.SESSION, member);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        if (!success) result.put("message", "프로필 수정에 실패했습니다.");
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/mobile/mypage
     * 회원 탈퇴 — 모든 관련 데이터 soft delete 후 세션 무효화
     */
    @DeleteMapping("/api/mobile/mypage")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobileDeleteAccount(HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();

        try {
            boolean success = memberService.deleteMember(member.getMember_seq());
            if (success) session.invalidate();
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.status(500).body(err);
        }
    }

    /**
     * GET /api/mobile/mypage/bookclubs
     * 내가 가입한 독서모임 목록
     */
    @GetMapping("/api/mobile/mypage/bookclubs")
    @ResponseBody
    public ResponseEntity<List<BookClubVO>> mobileMyBookClubs(HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();
        List<BookClubVO> clubs = bookClubService.getMyBookClubs(member.getMember_seq());
        return ResponseEntity.ok(clubs);
    }

    /**
     * GET /api/mobile/mypage/bookclubs/wished
     * 내가 찜한 독서모임 목록
     */
    @GetMapping("/api/mobile/mypage/bookclubs/wished")
    @ResponseBody
    public ResponseEntity<List<BookClubVO>> mobileMyWishedBookClubs(HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);
        if (member == null) return ResponseEntity.status(401).build();
        List<BookClubVO> clubs = bookClubService.getWishBookClubs(member.getMember_seq());
        return ResponseEntity.ok(clubs);
    }

    // ─── 공지사항 ─────────────────────────────────────────────────────────────

    /**
     * GET /api/mobile/notices
     * 활성 공지사항 목록 (페이징)
     * 응답: { notices: List<NoticeVO>, total, page, hasMore }
     */
    @GetMapping("/api/mobile/notices")
    @ResponseBody
    public Map<String, Object> mobileNoticeList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        project.util.paging.SearchVO searchVO = new project.util.paging.SearchVO();
        searchVO.setPage(page);
        searchVO.setSize(size);

        List<project.admin.NoticeVO> notices = adminService.selectActiveNotices(searchVO);
        int total = adminService.countActiveNotices(searchVO);

        Map<String, Object> result = new HashMap<>();
        result.put("notices", notices);
        result.put("total", total);
        result.put("page", page);
        result.put("hasMore", (searchVO.getOffset() + notices.size()) < total);
        return result;
    }

    /**
     * GET /api/mobile/notices/{noticeSeq}
     * 공지사항 상세 조회 (조회수 증가 포함)
     */
    @GetMapping("/api/mobile/notices/{noticeSeq}")
    @ResponseBody
    public ResponseEntity<project.admin.NoticeVO> mobileNoticeDetail(@PathVariable Long noticeSeq) {
        project.admin.NoticeVO notice = adminService.selectNotice(noticeSeq);
        if (notice == null) return ResponseEntity.status(404).build();
        adminService.increaseViewCount(noticeSeq);
        return ResponseEntity.ok(notice);
    }

    // ─── 내부 유틸 ───────────────────────────────────────────────────────────

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("Proxy-Client-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
            ip = request.getRemoteAddr();
        if (ip != null && ip.indexOf(",") > 0)
            ip = ip.substring(0, ip.indexOf(","));
        if (ip != null) {
            ip = ip.trim();
            if (!ip.matches("^[0-9a-fA-F.:]+$")) ip = "0.0.0.0";
        }
        return ip;
    }
}
