package project.bookclub;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpSession;
//
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.admin.AdminVO;
import project.bookclub.ENUM.JoinRequestResult;
import project.bookclub.dto.BookClubPageResponseDTO;
import project.bookclub.vo.BookClubBoardVO;
import project.bookclub.vo.BookClubVO;
import project.member.MemberVO;
import project.util.exception.ClientException;
import project.util.exception.bookclub.BookClubNotFoundException;
import project.util.logInOut.LoginUtil;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/bookclubs")
public class BookClubController {

    private final BookClubService bookClubService;

    @org.springframework.beans.factory.annotation.Value("${api.kakao.map.js-key}")
    private String kakaoJsKey;

    /**
     * 독서모임 상세 페이지 공통 model 세팅 (조회 로직 재사용)
     * - fragment 엔드포인트에서도 동일한 model 데이터 필요
     * - 비즈니스 로직 변경 없이 조회 로직만 재사용
     */
    private void loadBookClubDetailModel(Long bookClubId, HttpSession session, Model model) {
        // 1. 모임 조회
        BookClubVO bookClub = bookClubService.getBookClubById(bookClubId);

        // 2. 조회 결과 없음 또는 삭제된 모임 처리
        if (bookClub == null || bookClub.getBook_club_deleted_dt() != null) {
            model.addAttribute("errorMessage", "존재하지 않거나 삭제된 모임입니다.");
            return;
        }

        // 3. 세션에서 로그인 멤버 정보 가져오기
        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");

        boolean isLogin = (loginMember != null);

        model.addAttribute("isLogin", isLogin);

        // 4. 로그인 상태일 때만 추가 상태 계산
        if (isLogin) {
            Long loginMemberSeq = loginMember.getMember_seq();

            model.addAttribute("loginMemberSeq", loginMemberSeq);

            // 4-1. 모임장 여부 판단
            boolean isLeader = Objects.equals(bookClub.getBook_club_leader_seq(), loginMemberSeq);
            model.addAttribute("isLeader", isLeader);

            // 4-2. 멤버 여부 판단 (JOINED 상태)
            boolean isMember = bookClubService.isMemberJoined(bookClubId, loginMemberSeq);
            model.addAttribute("isMember", isMember);

            // 4-3. 대기중인 가입 신청 여부 판단
            boolean hasPendingRequest = bookClubService.hasPendingRequest(bookClubId, loginMemberSeq);
            model.addAttribute("hasPendingRequest", hasPendingRequest);

            // 4-4. 거절된 가입 신청 여부 판단
            boolean hasRejectedRequest = bookClubService.hasRejectedRequest(bookClubId, loginMemberSeq);
            model.addAttribute("hasRejectedRequest", hasRejectedRequest);

            // 4-5. 찜 여부 판단
            boolean isWished = bookClubService.isWished(bookClubId, loginMemberSeq);
            model.addAttribute("isWished", isWished);

            // 4-6. CTA 상태 계산 (우선순위: JOINED > WAIT > REJECTED > 신청 가능)
            // - isLeader는 별도로 "모임 관리하기" 버튼 표시에 사용 가능
            // - CTA는 가입/탈퇴/대기/재신청 상태를 나타냄
            String ctaStatus;
            if (isMember) {
                ctaStatus = "JOINED"; // 탈퇴하기 버튼
            } else if (hasPendingRequest) {
                ctaStatus = "WAIT"; // 승인 대기중 문구 (버튼 비활성)
            } else if (hasRejectedRequest) {
                ctaStatus = "REJECTED"; // 다시 신청하기 버튼
            } else {
                ctaStatus = "NONE"; // 가입 신청하기 버튼
            }
            model.addAttribute("ctaStatus", ctaStatus);
        } else {
            // 비로그인 시 기본값 설정
            model.addAttribute("isLeader", false);
            model.addAttribute("isMember", false);
            model.addAttribute("hasPendingRequest", false);
            model.addAttribute("hasRejectedRequest", false);
            model.addAttribute("isWished", false);
            model.addAttribute("ctaStatus", "NONE"); // 비로그인은 가입 신청하기만 표시
        }

        // 5. 현재 참여 인원 수 조회
        int joinedMemberCount = bookClubService.getTotalJoinedMemberCount(bookClubId);
        model.addAttribute("joinedMemberCount", joinedMemberCount);

        // 6. 찜 개수 조회
        int wishCount = bookClubService.getWishCount(bookClubId);
        model.addAttribute("wishCount", wishCount);

        // 7. 멤버 목록 조회 (홈 탭 - 함께하는 멤버)
        var members = bookClubService.getJoinedMembersForManage(bookClubId);
        model.addAttribute("members", members);

        // 8. Model에 데이터 담기
        model.addAttribute("bookClub", bookClub);
    }

    /*
     * 독서모임 메인
     * getBookClubs : keyword 없이 모임 전체 조회
     * searchBookClubs : keyword로 모임 검색
     */

    @GetMapping
    public String getBookClubs(Model model, HttpSession session) {
        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        Long memberSeq = (loginMember != null) ? loginMember.getMember_seq() : null;

        List<BookClubVO> bookClubs = bookClubService.getBookClubList(memberSeq);

        model.addAttribute("bookclubList", bookClubs);
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        return "bookclub/bookclub_list";
    }

    @GetMapping("/search")
    @ResponseBody
    public BookClubPageResponseDTO searchBookClubs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "latest") String sort,
            @RequestParam(required = false, defaultValue = "0") int page,
            HttpSession session) {
        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        Long memberSeq = (loginMember != null) ? loginMember.getMember_seq() : null;

        BookClubPageResponseDTO response = bookClubService.searchBookClubs(keyword, sort, page, memberSeq);

        return response;
    }

    @PostMapping
    @ResponseBody
    public Map<String, Object> createBookClubs(
            @Valid @ModelAttribute BookClubVO vo,
            BindingResult bindingResult,
            @RequestParam(value = "banner_img", required = false) MultipartFile bannerImg,
            HttpSession session) {

        log.info("vo = {}", vo);
        log.info("banner_img = {}", vo.getBanner_img_url());

        MemberVO loginUser = (MemberVO) session.getAttribute("loginSess");

        if (loginUser == null) {
            return Map.of(
                    "status", "fail",
                    "message", "LOGIN_REQUIRED");
        }

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .findFirst()
                    .orElse("입력값을 확인해주세요.");
            return Map.of("status", "fail", "message", errorMsg);
        }

        vo.setBook_club_leader_seq(loginUser.getMember_seq());

        // 이미지 파일 처리 (디버그 로그 추가)
        log.info("배너 이미지 업로드 체크: null={}, empty={}, originalFilename={}, size={}",
                bannerImg == null,
                bannerImg != null ? bannerImg.isEmpty() : "N/A",
                bannerImg != null ? bannerImg.getOriginalFilename() : "N/A",
                bannerImg != null ? bannerImg.getSize() : "N/A");

        if (bannerImg != null && !bannerImg.isEmpty()) {
            try {
                String s3Url = saveFile(bannerImg);
                vo.setBanner_img_url(s3Url);  // S3 전체 URL 저장
                log.info("Banner image uploaded to S3: {}", s3Url);
            } catch (ClientException e) {
                log.error("Failed to upload banner image to S3", e);
                return Map.of(
                        "status", "fail",
                        "message", "이미지 업로드에 실패했습니다.");
            }
        } else if (vo.getBanner_img_url() == null || vo.getBanner_img_url().trim().isEmpty()) {
            // 이미지 업로드도 없고 URL도 입력하지 않았다면 Service에서 랜덤 기본 이미지 선택
            // Service의 createBookClub에서 자동으로 6개 중 1개를 랜덤 선택하여 설정함
            log.info("No banner provided, Service will set random default banner");
        }

        try {
            bookClubService.createBookClub(vo);
            return Map.of("status", "ok");
        } catch (ClientException e) {
            log.warn("모임 생성 실패: {}", e.getMessage());
            return Map.of(
                    "status", "fail",
                    "message", e.getMessage());
        }
    }

    /**
     * 독서모임 상세 페이지 (2단계: 버튼 분기/상태 계산)
     * GET /bookclubs/{bookClubId}
     * - 모임 1건 조회 + JSP 출력
     * - 로그인 여부, 모임장/멤버 판단 로직 추가
     * - JSP에서 버튼 분기 처리 가능하도록 model 데이터 제공
     */
    @GetMapping("/{bookClubId}")
    public String getBookClubDetail(
            @PathVariable("bookClubId") Long bookClubId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // 종료된 모임 가드
        BookClubVO bookClub = bookClubService.getBookClubById(bookClubId);
        if (bookClub == null || bookClub.getBook_club_deleted_dt() != null) {
            MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
            Long memberSeq = (loginMember != null ? loginMember.getMember_seq() : null);

            log.warn("종료된 모임 상세 GET 접근: bookClubId={}, memberSeq={}", bookClubId, memberSeq);

            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않거나 종료된 모임입니다.");
            return "redirect:/bookclubs";
        }

        // 공통 model 세팅 메서드 호출
        loadBookClubDetailModel(bookClubId, session, model);
        return "bookclub/bookclub_detail";
    }

    /**
     * 독서모임 상세 페이지 - 게시판 탭 fragment
     * GET /bookclubs/{bookClubId}/board-fragment
     * - fetch로 호출되어 게시판 탭 본문만 반환
     * - 동일한 model 세팅 재사용 (fragment에서도 bookClub 등 필요)
     * - 게시판 목록 조회 추가 (최근 원글 10개)
     *
     * [권한 가드]
     * - 비로그인: forbidden fragment 반환 (redirect 금지)
     * - 로그인했지만 JOINED 아님 또는 WAIT 상태: forbidden fragment 반환
     * - 모임장 또는 JOINED 멤버: 게시판 목록 정상 반환
     */
    @GetMapping("/{bookClubId}/board-fragment")
    public String getBoardFragment(
            @PathVariable("bookClubId") Long bookClubId,
            HttpSession session,
            Model model) {

        model.addAttribute("bookClubId", bookClubId);

        // 1. 로그인 여부 확인
        AdminVO adminVO = (AdminVO) session.getAttribute("adminSess");
        boolean isAdmin = (adminVO != null);
        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        if (loginMember == null && !isAdmin) {
            // fragment에서는 redirect 불가 → forbidden 처리
            return "bookclub/bookclub_board_forbidden";
        }

        Long loginMemberSeq = (loginMember != null) ? loginMember.getMember_seq() : null;

        // 2. 모임 조회
        BookClubVO bookClub = bookClubService.getBookClubById(bookClubId);
        if (bookClub == null || bookClub.getBook_club_deleted_dt() != null) {
            log.warn("종료된 모임 게시판 fragment 접근: bookClubId={}, memberSeq={}", bookClubId, loginMemberSeq);
            // 종료된 모임 전용 fragment 반환
            return "bookclub/bookclub_closed_fragment";
        }

        // 3. 권한 판정 (모임장 또는 JOINED 멤버만)
        boolean isLeader = (loginMemberSeq != null) && Objects.equals(bookClub.getBook_club_leader_seq(), loginMemberSeq);
        boolean isMember = (loginMemberSeq != null) && bookClubService.isMemberJoined(bookClubId, loginMemberSeq);

        if (!isAdmin && !isLeader && !isMember) {
            // 권한 없음 - forbidden fragment 반환
            model.addAttribute("isLogin", true);
            return "bookclub/bookclub_board_forbidden";
        }

        // 4. 권한 있음: 공통 model 세팅 + 게시판 목록 조회
        loadBookClubDetailModel(bookClubId, session, model);

        // 게시판 목록 조회 (최근 원글 10개)
        List<BookClubBoardVO> boards = bookClubService.getRecentBoards(bookClubId);
        model.addAttribute("boards", boards);

        return "bookclub/bookclub_detail_board";
    }

    /**
     * 권한 검증 공통 메서드
     *
     * @return 권한 있으면 null, 없으면 forbidden view 이름 또는 redirect
     */
    private String checkBoardAccessPermission(Long bookClubId, Long postId, HttpSession session, Model model,
                                              RedirectAttributes redirectAttributes) {
        model.addAttribute("bookClubId", bookClubId);

        // 1. 로그인 여부 확인
        AdminVO adminVO = (AdminVO) session.getAttribute("adminSess");
        boolean isAdmin = (adminVO != null);

        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        if (loginMember == null && !isAdmin) {
            return LoginUtil.redirectToLogin();
        }

        Long loginMemberSeq = (loginMember != null) ? loginMember.getMember_seq() : null;

        // 2. 모임 조회
        BookClubVO bookClub = bookClubService.getBookClubById(bookClubId);
        if (bookClub == null || bookClub.getBook_club_deleted_dt() != null) {
            log.warn("종료된 모임 게시글 GET 접근: bookClubId={}, postId={}, memberSeq={}", bookClubId, postId, loginMemberSeq);
            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않거나 종료된 모임입니다.");
            return "redirect:/bookclubs";
        }

        // 3. 권한 판정
        boolean isLeader = (loginMemberSeq != null) && Objects.equals(bookClub.getBook_club_leader_seq(), loginMemberSeq);
        boolean isMember = (loginMemberSeq != null) && bookClubService.isMemberJoined(bookClubId, loginMemberSeq);
        boolean hasPendingRequest = (loginMemberSeq != null) && bookClubService.hasPendingRequest(bookClubId, loginMemberSeq);

        boolean allow = isAdmin || (isLeader || isMember) && !hasPendingRequest;

        // model에 권한 정보 담기
        model.addAttribute("isLogin", true);
        model.addAttribute("loginMemberSeq", loginMemberSeq);
        model.addAttribute("isLeader", isLeader);
        model.addAttribute("isMember", isMember);
        model.addAttribute("canWriteComment", allow);

        return allow ? null : "bookclub/bookclub_post_forbidden";
    }

    /**
     * 독서모임 게시글 상세 페이지
     * GET /bookclubs/{bookClubId}/posts/{postId}
     * - 게시글 단건 조회 (본문 렌더링)
     * - 댓글 목록 조회 추가 (SELECT만)
     *
     * [권한 가드]
     * - 비로그인: /login으로 redirect
     * - 로그인했지만 JOINED 아님 또는 WAIT 상태: forbidden 풀 페이지
     * - 모임장 또는 JOINED 멤버: 게시글 상세 정상 반환
     */
    @GetMapping("/{bookClubId}/posts/{postId}")
    public String getPostDetail(
            @PathVariable("bookClubId") Long bookClubId,
            @PathVariable("postId") Long postId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // 권한 검증 (공통 메서드 재사용)
        String permissionCheckResult = checkBoardAccessPermission(bookClubId, postId, session, model,
                redirectAttributes);
        if (permissionCheckResult != null) {
            return permissionCheckResult;
        }

        // 권한 있음: 게시글 + 댓글 조회
        BookClubBoardVO post = bookClubService.getBoardDetail(bookClubId, postId);

        // 게시글 없음 처리
        if (post == null) {
            throw new BookClubNotFoundException("게시글을 찾을 수 없거나 삭제되었습니다.");
        }

        // 댓글 목록 조회
        List<BookClubBoardVO> comments = bookClubService.getBoardComments(bookClubId, postId);

        // 좋아요 여부 설정 (로그인 사용자)
        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        AdminVO loginAdmin = (AdminVO) session.getAttribute("adminSess");

        if (loginAdmin == null && loginMember != null) {
            Long memberSeq = loginMember.getMember_seq();
            // 게시글 좋아요 여부
            post.setIs_liked(bookClubService.isBoardLiked(post.getBook_club_board_seq(), memberSeq));
            // 댓글 좋아요 여부
            for (BookClubBoardVO comment : comments) {
                comment.setIs_liked(bookClubService.isBoardLiked(comment.getBook_club_board_seq(), memberSeq));
            }
        }

        // model에 데이터 담기
        model.addAttribute("post", post);
        model.addAttribute("comments", comments);

        return "bookclub/bookclub_post_detail";
    }

    /**
     * 댓글 작성 (PRG 패턴)
     * POST /bookclubs/{bookClubId}/posts/{postId}/comments
     * - 로그인 필수
     * - 모임장 또는 JOINED 멤버만 작성 가능
     * - 부모글 검증 (우회 방지)
     */
    @PostMapping("/{bookClubId}/posts/{postId}/comments")
    public String createComment(
            @PathVariable("bookClubId") Long bookClubId,
            @PathVariable("postId") Long postId,
            @RequestParam("commentCont") String commentCont,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String redirectUrl = "redirect:/bookclubs/" + bookClubId + "/posts/" + postId + "#comments";

        // 1. 로그인 확인
        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        if (loginMember == null) {
            return LoginUtil.redirectToLogin();
        }

        Long memberSeq = loginMember.getMember_seq();

        // 2. 권한 체크 (모임장 OR JOINED 멤버)
        BookClubVO bookClub = bookClubService.getBookClubById(bookClubId);
        if (bookClub == null || bookClub.getBook_club_deleted_dt() != null) {
            log.warn("종료된 모임 댓글 작성 시도: bookClubId={}, memberSeq={}", bookClubId, memberSeq);
            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않거나 종료된 모임입니다.");
            return "redirect:/bookclubs";
        }

        boolean isLeader = Objects.equals(bookClub.getBook_club_leader_seq(), memberSeq);
        boolean isMember = bookClubService.isMemberJoined(bookClubId, memberSeq);

        if (!isLeader && !isMember) {
            // 권한 없음 - 모임 상세 페이지로 리다이렉트
            redirectAttributes.addFlashAttribute("errorMessage", "댓글을 작성할 권한이 없습니다.");
            return "redirect:/bookclubs/" + bookClubId;
        }

        // 3. 댓글 내용 검증
        if (commentCont == null || commentCont.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "댓글 내용을 입력해주세요.");
            return redirectUrl;
        }

        // 4. 부모글 검증 (우회 방지)
        boolean isValidPost = bookClubService.existsRootPost(bookClubId, postId);
        if (!isValidPost) {
            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 게시글입니다.");
            return redirectUrl;
        }

        // 5. 댓글 INSERT
        bookClubService.createBoardComment(bookClubId, postId, memberSeq, commentCont);

        // 6. 성공 시 게시글 상세 페이지로 리다이렉트
        return redirectUrl;
    }

    /**
     * 댓글 수정
     * POST /bookclubs/{bookClubId}/posts/{postId}/comments/{commentId}/edit
     * - 로그인 필수
     * - 댓글 작성자만 수정 가능
     */
    @PostMapping("/{bookClubId}/posts/{postId}/comments/{commentId}/edit")
    public String updateComment(
            @PathVariable("bookClubId") Long bookClubId,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @RequestParam("commentCont") String commentCont,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String redirectUrl = "redirect:/bookclubs/" + bookClubId + "/posts/" + postId + "#comments";

        // 1. 로그인 확인
        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        if (loginMember == null) {
            return LoginUtil.redirectToLogin();
        }
        Long memberSeq = loginMember.getMember_seq();

        // 1-1. 종료된 모임 가드
        BookClubVO bookClub = bookClubService.getBookClubById(bookClubId);
        if (bookClub == null || bookClub.getBook_club_deleted_dt() != null) {
            log.warn("종료된 모임 댓글 수정 시도: bookClubId={}, commentId={}, memberSeq={}", bookClubId, commentId, memberSeq);
            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않거나 종료된 모임입니다.");
            return "redirect:/bookclubs";
        }

        // 2. 댓글 조회 및 권한 확인
        var comment = bookClubService.getBoardCommentById(commentId);
        if (comment == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 댓글입니다.");
            return redirectUrl;
        }

        // 작성자만 수정 가능
        if (!Objects.equals(comment.getMember_seq(), memberSeq)) {
            redirectAttributes.addFlashAttribute("errorMessage", "본인이 작성한 댓글만 수정할 수 있습니다.");
            return redirectUrl;
        }

        // 3. 댓글 내용 검증
        if (commentCont == null || commentCont.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "댓글 내용을 입력해주세요.");
            return redirectUrl;
        }

        // 4. 댓글 UPDATE
        bookClubService.updateBoardComment(commentId, commentCont);

        return redirectUrl;
    }

    /**
     * 댓글 삭제
     * POST /bookclubs/{bookClubId}/posts/{postId}/comments/{commentId}/delete
     * - 로그인 필수
     * - 댓글 작성자 또는 모임장만 삭제 가능
     */
    @PostMapping("/{bookClubId}/posts/{postId}/comments/{commentId}/delete")
    public String deleteComment(
            @PathVariable("bookClubId") Long bookClubId,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String redirectUrl = "redirect:/bookclubs/" + bookClubId + "/posts/" + postId + "#comments";

        // 1. 로그인 확인
        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        if (loginMember == null) {
            return LoginUtil.redirectToLogin();
        }
        Long memberSeq = loginMember.getMember_seq();

        // 1-1. 종료된 모임 가드
        BookClubVO bookClub = bookClubService.getBookClubById(bookClubId);
        if (bookClub == null || bookClub.getBook_club_deleted_dt() != null) {
            log.warn("종료된 모임 댓글 삭제 시도: bookClubId={}, commentId={}, memberSeq={}", bookClubId, commentId, memberSeq);
            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않거나 종료된 모임입니다.");
            return "redirect:/bookclubs";
        }

        // 2. 댓글 조회
        var comment = bookClubService.getBoardCommentById(commentId);
        if (comment == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 댓글입니다.");
            return redirectUrl;
        }

        // 3. 권한 확인 (작성자 또는 모임장)
        boolean isAuthor = Objects.equals(comment.getMember_seq(), memberSeq);
        boolean isLeader = bookClubService.isLeader(bookClubId, memberSeq);

        if (!isAuthor && !isLeader) {
            redirectAttributes.addFlashAttribute("errorMessage", "댓글을 삭제할 권한이 없습니다.");
            return redirectUrl;
        }

        // 4. 댓글 DELETE (soft delete)
        bookClubService.deleteBoardComment(commentId);

        return redirectUrl;
    }

    /**
     * 독서모임 가입 신청 (승인형) - 개선판
     * POST /bookclubs/{bookClubId}/join-requests
     *
     * 개선사항:
     * - Controller에서 비즈니스 검증(isMemberJoined) 제거 → Service로 이동
     * - try-catch 예외 처리 제거 → enum 결과로 통일
     * - Flash 메시지 추가 (enum.getMessage() 활용)
     */
    @PostMapping("/{bookClubId}/join-requests")
    public String createJoinRequest(
            @PathVariable("bookClubId") Long bookClubId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        // 1. 로그인 확인 (Controller 책임 유지)
        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        if (loginMember == null) {
            log.warn("비로그인 상태에서 가입 신청 시도: bookClubId={}", bookClubId);
            return LoginUtil.redirectToLogin();
        }

        // 1-1. 종료된 모임 가드
        BookClubVO bookClub = bookClubService.getBookClubById(bookClubId);
        if (bookClub == null || bookClub.getBook_club_deleted_dt() != null) {
            log.warn("종료된 모임 가입 신청 시도: bookClubId={}, memberSeq={}", bookClubId, loginMember.getMember_seq());
            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않거나 종료된 모임입니다.");
            return "redirect:/bookclubs";
        }

        // 2. Service 호출 → enum 결과 받기 (비즈니스 로직은 Service에 위임)
        JoinRequestResult result = bookClubService.createJoinRequest(
                bookClubId,
                loginMember.getMember_seq(),
                null);

        // 3. enum 기반 분기 처리 (간결하고 명확)
        switch (result) {
            case SUCCESS:
                redirectAttributes.addFlashAttribute("successMessage", result.getMessage());
                break;

            case ALREADY_JOINED:
            case ALREADY_REQUESTED:
            case INVALID_PARAMETERS:
                redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
                break;
        }

        return "redirect:/bookclubs/" + bookClubId;
    }

    /**
     * 독서모임 가입 신청 (AJAX용)
     * POST /bookclubs/{bookClubId}/join
     */
    @PostMapping("/{bookClubId}/join")
    @ResponseBody
    public Map<String, Object> createJoinRequestAjax(
            @PathVariable("bookClubId") Long bookClubId,
            @RequestBody(required = false) Map<String, String> body,
            HttpSession session) {

        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        if (loginMember == null) {
            return Map.of("status", "fail", "message", "로그인이 필요합니다.");
        }

        // 종료된 모임 가드
        BookClubVO bookClub = bookClubService.getBookClubById(bookClubId);
        if (bookClub == null || bookClub.getBook_club_deleted_dt() != null) {
            log.warn("종료된 모임 가입 시도: bookClubId={}, memberSeq={}", bookClubId, loginMember.getMember_seq());
            return Map.of("status", "fail", "message", "존재하지 않거나 종료된 모임입니다.");
        }

        String reason = (body != null) ? body.get("reason") : null;

        JoinRequestResult result = bookClubService.createJoinRequest(
                bookClubId,
                loginMember.getMember_seq(),
                reason);

        if (result == JoinRequestResult.SUCCESS) {
            return Map.of("status", "ok", "message", result.getMessage());
        } else {
            return Map.of("status", "fail", "message", result.getMessage());
        }
    }

    /**
     * 독서모임 찜 토글 (AJAX용)
     * POST /bookclubs/{bookClubId}/wish
     * - 로그인 필수
     * - 찜 상태 토글 (찜 ↔ 찜 해제)
     */
    @PostMapping("/{bookClubId}/wish")
    @ResponseBody
    public Map<String, Object> toggleWish(
            @PathVariable("bookClubId") Long bookClubId,
            HttpSession session) {

        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        if (loginMember == null) {
            return Map.of("status", "fail", "message", "로그인이 필요합니다.", "needLogin", true);
        }

        // 찜 토글 실행
        boolean isWished = bookClubService.toggleWish(bookClubId, loginMember.getMember_seq());

        // 새로운 찜 개수 조회
        int wishCount = bookClubService.getWishCount(bookClubId);

        return Map.of(
                "status", "ok",
                "wished", isWished,
                "wishCount", wishCount,
                "message", isWished ? "찜 목록에 추가되었습니다." : "찜 목록에서 제거되었습니다.");
    }

    /**
     * 독서모임 탈퇴 (AJAX용)
     * POST /bookclubs/{bookClubId}/leave
     *
     * 검증 순서:
     * 1. 로그인 확인
     * 2. 모임 존재 여부 확인
     * 3. Service 레이어에서 비즈니스 로직 처리:
     * - 일반 멤버: join_st='LEFT' 업데이트
     * - 모임장: 자동 승계 또는 모임 종료
     *
     * @return JSON {success, message, leaderChanged?, newLeaderSeq?, clubClosed?,
     *         ctaStatus}
     */
    @PostMapping("/{bookClubId}/leave")
    @ResponseBody
    public Map<String, Object> leaveBookClub(
            @PathVariable("bookClubId") Long bookClubId,
            HttpSession session) {

        // 1. 로그인 확인
        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        if (loginMember == null) {
            log.warn("비로그인 상태에서 탈퇴 시도: bookClubId={}", bookClubId);
            return Map.of("success", false, "message", "로그인이 필요합니다.");
        }

        Long loginMemberSeq = loginMember.getMember_seq();

        // 2. 모임 조회
        BookClubVO bookClub = bookClubService.getBookClubById(bookClubId);
        if (bookClub == null || bookClub.getBook_club_deleted_dt() != null) {
            log.warn("종료된 모임 탈퇴 시도: bookClubId={}", bookClubId);
            return Map.of("success", false, "message", "존재하지 않거나 종료된 모임입니다.");
        }

        // 3. Service 호출 (비즈니스 로직 위임)
        try {
            Map<String, Object> result = bookClubService.leaveBookClub(bookClubId, loginMemberSeq);

            log.info("멤버 탈퇴 완료: bookClubId={}, memberSeq={}, result={}",
                    bookClubId, loginMemberSeq, result);

            return result;

        } catch (ClientException e) {
            log.warn("멤버 탈퇴 실패: bookClubId={}, memberSeq={}, error={}",
                    bookClubId, loginMemberSeq, e.getMessage());
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    /**
     * 게시글/댓글 좋아요 토글 (AJAX용)
     * POST /bookclubs/{bookClubId}/boards/{boardSeq}/like
     * - 로그인 필수
     * - 모임 멤버만 좋아요 가능
     * - 좋아요 상태 토글 (좋아요 ↔ 좋아요 해제)
     */
    @PostMapping("/{bookClubId}/boards/{boardSeq}/like")
    @ResponseBody
    public Map<String, Object> toggleBoardLike(
            @PathVariable("bookClubId") Long bookClubId,
            @PathVariable("boardSeq") Long boardSeq,
            HttpSession session) {

        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        if (loginMember == null) {
            return Map.of("status", "fail", "message", "로그인이 필요합니다.", "needLogin", true);
        }

        Long memberSeq = loginMember.getMember_seq();

        // 모임 멤버 여부 확인
        BookClubVO bookClub = bookClubService.getBookClubById(bookClubId);
        if (bookClub == null || bookClub.getBook_club_deleted_dt() != null) {
            return Map.of("status", "fail", "message", "존재하지 않거나 종료된 모임입니다.");
        }

        boolean isLeader = Objects.equals(bookClub.getBook_club_leader_seq(), memberSeq);
        boolean isMember = bookClubService.isMemberJoined(bookClubId, memberSeq);

        if (!isLeader && !isMember) {
            return Map.of("status", "fail", "message", "모임 멤버만 좋아요할 수 있습니다.");
        }

        // 게시글이 해당 모임에 속하는지 검증 (Cross-Club IDOR 방지)
        if (bookClubService.getBoardDetail(bookClubId, boardSeq) == null) {
            return Map.of("status", "fail", "message", "해당 모임의 게시글이 아닙니다.");
        }

        // 좋아요 토글 실행
        boolean isLiked = bookClubService.toggleBoardLike(boardSeq, memberSeq);

        // 새로운 좋아요 개수 조회
        int likeCount = bookClubService.getBoardLikeCount(boardSeq);

        return Map.of(
                "status", "ok",
                "liked", isLiked,
                "likeCount", likeCount,
                "message", isLiked ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다.");
    }

    /**
     * 이미지 파일 S3 업로드
     * 이미지 파일만 허용 (jpg, jpeg, png, gif, webp)
     * @return S3 전체 URL (예: https://secondarybooksimages.s3.ap-northeast-2.amazonaws.com/images/{UUID}.jpg)
     */
    private String saveFile(MultipartFile file) {
        return bookClubService.uploadFile(file);
    }

    /**
     * 게시글 작성 폼 페이지
     * GET /bookclubs/{bookClubId}/posts
     * - 로그인 필수
     * - 모임장 또는 JOINED 멤버만 접근 가능
     */
    @GetMapping("/{bookClubId}/posts")
    public String createPostForm(
            @PathVariable("bookClubId") Long bookClubId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // 권한 검증
        String permissionCheckResult = checkBoardAccessPermission(bookClubId, null, session, model, redirectAttributes);
        if (permissionCheckResult != null) {
            return permissionCheckResult;
        }

        // 모임 정보 조회
        BookClubVO bookClub = bookClubService.getBookClubById(bookClubId);
        model.addAttribute("bookClub", bookClub);
        model.addAttribute("bookClubId", bookClubId);

        return "bookclub/bookclub_posts";
    }

    /**
     * 게시글 작성 처리 (PRG 패턴)
     * POST /bookclubs/{bookClubId}/posts
     * - 로그인 필수
     * - 모임장 또는 JOINED 멤버만 작성 가능
     * - 제목, 내용 필수 / 이미지, 책 선택은 선택사항
     */
    @PostMapping("/{bookClubId}/posts")
    public String createPost(
            @PathVariable("bookClubId") Long bookClubId,
            @RequestParam("boardTitle") String boardTitle,
            @RequestParam("boardCont") String boardCont,
            @RequestParam(value = "boardImage", required = false) MultipartFile boardImage,
            @RequestParam(value = "isbn", required = false) String isbn,
            @RequestParam(value = "bookTitle", required = false) String bookTitle,
            @RequestParam(value = "bookAuthor", required = false) String bookAuthor,
            @RequestParam(value = "bookImgUrl", required = false) String bookImgUrl,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 1. 로그인 확인
        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        if (loginMember == null) {
            return LoginUtil.redirectToLogin();
        }

        Long memberSeq = loginMember.getMember_seq();

        // 2. 권한 체크 (모임장 OR JOINED 멤버)
        BookClubVO bookClub = bookClubService.getBookClubById(bookClubId);
        if (bookClub == null || bookClub.getBook_club_deleted_dt() != null) {
            log.warn("종료된 모임 게시글 작성 시도: bookClubId={}, memberSeq={}", bookClubId, memberSeq);
            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않거나 종료된 모임입니다.");
            return "redirect:/bookclubs";
        }

        boolean isLeader = Objects.equals(bookClub.getBook_club_leader_seq(), memberSeq);
        boolean isMember = bookClubService.isMemberJoined(bookClubId, memberSeq);

        if (!isLeader && !isMember) {
            redirectAttributes.addFlashAttribute("errorMessage", "게시글을 작성할 권한이 없습니다.");
            return "redirect:/bookclubs/" + bookClubId;
        }

        // 3. 필수 입력값 검증
        if (boardTitle == null || boardTitle.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "제목을 입력해주세요.");
            return "redirect:/bookclubs/" + bookClubId + "/posts";
        }

        if (boardCont == null || boardCont.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "내용을 입력해주세요.");
            return "redirect:/bookclubs/" + bookClubId + "/posts";
        }

        // 4. 이미지 파일 처리
        String savedImageUrl = null;
        if (boardImage != null && !boardImage.isEmpty()) {
            try {
                savedImageUrl = saveFile(boardImage);  // S3 URL 직접 저장
                log.info("Board image uploaded to S3: {}", savedImageUrl);
            } catch (ClientException e) {
                log.error("Failed to upload board image to S3", e);
                redirectAttributes.addFlashAttribute("errorMessage", "이미지 업로드에 실패했습니다.");
                return "redirect:/bookclubs/" + bookClubId + "/posts";
            }
        }

        // 5. VO 생성 및 INSERT
        BookClubBoardVO boardVO = new BookClubBoardVO();
        boardVO.setBook_club_seq(bookClubId);
        boardVO.setMember_seq(memberSeq);
        boardVO.setBoard_title(boardTitle);
        boardVO.setBoard_cont(boardCont);
        boardVO.setBoard_img_url(savedImageUrl);
        // 책 정보 (선택사항)
        boardVO.setIsbn(isbn);
        boardVO.setBook_title(bookTitle);
        boardVO.setBook_author(bookAuthor);
        boardVO.setBook_img_url(bookImgUrl);

        Long newPostId = bookClubService.createBoardPost(boardVO);

        // 6. 성공 시 게시글 상세 페이지로 리다이렉트
        redirectAttributes.addFlashAttribute("successMessage", "게시글이 등록되었습니다.");
        return "redirect:/bookclubs/" + bookClubId + "/posts/" + newPostId;
    }

    /**
     * 게시글 수정 폼 페이지
     * GET /bookclubs/{bookClubId}/posts/{postId}/edit
     * - 로그인 필수
     * - 작성자만 접근 가능
     */
    @GetMapping("/{bookClubId}/posts/{postId}/edit")
    public String editPostForm(
            @PathVariable("bookClubId") Long bookClubId,
            @PathVariable("postId") Long postId,
            HttpSession session,
            Model model) {

        // 1. 로그인 확인
        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        if (loginMember == null) {
            return LoginUtil.redirectToLogin();
        }

        Long memberSeq = loginMember.getMember_seq();

        // 1-1. 종료된 모임 가드
        BookClubVO bookClub = bookClubService.getBookClubById(bookClubId);
        if (bookClub == null || bookClub.getBook_club_deleted_dt() != null) {
            log.warn("종료된 모임 게시글 수정 폼 접근: bookClubId={}, postId={}, memberSeq={}", bookClubId, postId, memberSeq);
            model.addAttribute("errorMessage", "존재하지 않거나 종료된 모임입니다.");
            return "bookclub/bookclub_post_forbidden";
        }

        // 2. 게시글 조회
        BookClubBoardVO post = bookClubService.getBoardDetail(bookClubId, postId);
        if (post == null) {
            throw new BookClubNotFoundException("게시글을 찾을 수 없거나 삭제되었습니다.");
        }

        // 3. 작성자 확인 (수정은 작성자만 가능)
        if (!Objects.equals(post.getMember_seq(), memberSeq)) {
            model.addAttribute("errorMessage", "수정 권한이 없습니다.");
            return "bookclub/bookclub_post_forbidden";
        }

        // 4. 모임 정보 조회 (이미 위에서 조회했으므로 재사용)
        model.addAttribute("bookClub", bookClub);
        model.addAttribute("bookClubId", bookClubId);
        model.addAttribute("post", post);
        model.addAttribute("isEdit", true);

        return "bookclub/bookclub_posts_edit";
    }

    /**
     * 게시글 수정 처리 (PRG 패턴)
     * POST /bookclubs/{bookClubId}/posts/{postId}/edit
     * - 로그인 필수
     * - 작성자만 수정 가능
     */
    @PostMapping("/{bookClubId}/posts/{postId}/edit")
    public String editPost(
            @PathVariable("bookClubId") Long bookClubId,
            @PathVariable("postId") Long postId,
            @RequestParam("boardTitle") String boardTitle,
            @RequestParam("boardCont") String boardCont,
            @RequestParam(value = "boardImage", required = false) MultipartFile boardImage,
            @RequestParam(value = "keepExistingImage", required = false) String keepExistingImage,
            @RequestParam(value = "isbn", required = false) String isbn,
            @RequestParam(value = "bookTitle", required = false) String bookTitle,
            @RequestParam(value = "bookAuthor", required = false) String bookAuthor,
            @RequestParam(value = "bookImgUrl", required = false) String bookImgUrl,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 1. 로그인 확인
        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        if (loginMember == null) {
            return LoginUtil.redirectToLogin();
        }

        Long memberSeq = loginMember.getMember_seq();

        // 1-1. 종료된 모임 가드
        BookClubVO bookClub = bookClubService.getBookClubById(bookClubId);
        if (bookClub == null || bookClub.getBook_club_deleted_dt() != null) {
            log.warn("종료된 모임 게시글 수정 시도: bookClubId={}, postId={}, memberSeq={}", bookClubId, postId, memberSeq);
            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않거나 종료된 모임입니다.");
            return "redirect:/bookclubs";
        }

        // 2. 게시글 조회
        BookClubBoardVO existingPost = bookClubService.getBoardDetail(bookClubId, postId);
        if (existingPost == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "게시글을 찾을 수 없거나 삭제되었습니다.");
            return "redirect:/bookclubs/" + bookClubId;
        }

        // 3. 작성자 확인 (수정은 작성자만 가능)
        if (!Objects.equals(existingPost.getMember_seq(), memberSeq)) {
            redirectAttributes.addFlashAttribute("errorMessage", "수정 권한이 없습니다.");
            return "redirect:/bookclubs/" + bookClubId + "/posts/" + postId;
        }

        // 4. 필수 입력값 검증
        if (boardTitle == null || boardTitle.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "제목을 입력해주세요.");
            return "redirect:/bookclubs/" + bookClubId + "/posts/" + postId + "/edit";
        }

        if (boardCont == null || boardCont.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "내용을 입력해주세요.");
            return "redirect:/bookclubs/" + bookClubId + "/posts/" + postId + "/edit";
        }

        // 5. 이미지 파일 처리
        String oldImageUrl = existingPost.getBoard_img_url(); // 기존 이미지 URL (S3 삭제용)
        String savedImageUrl = oldImageUrl; // 기존 이미지 유지 기본값

        if (boardImage != null && !boardImage.isEmpty()) {
            // 새 이미지 업로드
            try {
                savedImageUrl = saveFile(boardImage);  // S3 URL 직접 저장
                log.info("Board image updated to S3: {}", savedImageUrl);
            } catch (ClientException e) {
                log.error("Failed to upload board image to S3", e);
                redirectAttributes.addFlashAttribute("errorMessage", "이미지 업로드에 실패했습니다.");
                return "redirect:/bookclubs/" + bookClubId + "/posts/" + postId + "/edit";
            }
        } else if (!"true".equals(keepExistingImage)) {
            // 기존 이미지 삭제 요청 (새 이미지 없고, 기존 유지 체크도 안 한 경우)
            savedImageUrl = null;
        }

        // 6. VO 생성 및 UPDATE
        BookClubBoardVO boardVO = new BookClubBoardVO();
        boardVO.setBook_club_seq(bookClubId);
        boardVO.setBook_club_board_seq(postId);
        boardVO.setBoard_title(boardTitle);
        boardVO.setBoard_cont(boardCont);
        boardVO.setBoard_img_url(savedImageUrl);
        // 책 정보 (선택사항)
        boardVO.setIsbn(isbn);
        boardVO.setBook_title(bookTitle);
        boardVO.setBook_author(bookAuthor);
        boardVO.setBook_img_url(bookImgUrl);

        boolean updated = bookClubService.updateBoardPost(boardVO, oldImageUrl);

        if (updated) {

            redirectAttributes.addFlashAttribute("successMessage", "게시글이 수정되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "게시글 수정에 실패했습니다.");
        }

        // 게시판 탭으로 리다이렉트
        return "redirect:/bookclubs/" + bookClubId + "?tab=board";
    }

    /**
     * 게시글 삭제 처리 (PRG 패턴)
     * POST /bookclubs/{bookClubId}/posts/{postId}/delete
     * - 로그인 필수
     * - 작성자 또는 모임장만 삭제 가능
     */
    @PostMapping("/{bookClubId}/posts/{postId}/delete")
    public String deletePost(
            @PathVariable("bookClubId") Long bookClubId,
            @PathVariable("postId") Long postId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 1. 로그인 확인
        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        if (loginMember == null) {
            return LoginUtil.redirectToLogin();
        }

        Long memberSeq = loginMember.getMember_seq();

        // 2. 모임 조회
        BookClubVO bookClub = bookClubService.getBookClubById(bookClubId);
        if (bookClub == null || bookClub.getBook_club_deleted_dt() != null) {
            log.warn("종료된 모임 게시글 삭제 시도: bookClubId={}, postId={}, memberSeq={}", bookClubId, postId, memberSeq);
            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않거나 종료된 모임입니다.");
            return "redirect:/bookclubs";
        }

        // 3. 게시글 조회
        BookClubBoardVO post = bookClubService.getBoardDetail(bookClubId, postId);
        if (post == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "게시글을 찾을 수 없거나 이미 삭제되었습니다.");
            return "redirect:/bookclubs/" + bookClubId + "?tab=board";
        }

        // 4. 삭제 권한 확인 (작성자 OR 모임장)
        boolean isAuthor = Objects.equals(post.getMember_seq(), memberSeq);
        boolean isLeader = Objects.equals(bookClub.getBook_club_leader_seq(), memberSeq);

        if (!isAuthor && !isLeader) {
            redirectAttributes.addFlashAttribute("errorMessage", "삭제 권한이 없습니다.");
            return "redirect:/bookclubs/" + bookClubId + "/posts/" + postId;
        }

        // 5. 삭제 처리 (soft delete)
        boolean deleted = bookClubService.deleteBoardPost(bookClubId, postId);

        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "게시글이 삭제되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "게시글 삭제에 실패했습니다.");
        }

        // 6. 게시판 탭으로 리다이렉트
        return "redirect:/bookclubs/" + bookClubId + "?tab=board";
    }
}
