package project.bookclub;

import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.bookclub.dto.BookClubManageViewDTO;
import project.bookclub.dto.BookClubUpdateSettingsDTO;
import project.bookclub.vo.BookClubVO;
import project.member.MemberVO;
import project.util.exception.ClientException;
import project.util.logInOut.LoginUtil;

/**
 * 독서모임 관리 컨트롤러 (모임장 전용)
 * - /manage 페이지 진입
 * - 가입 신청 승인/거절 (AJAX + JSON)
 * - 멤버 강퇴 (AJAX + JSON)
 * - 모임 설정 저장 (AJAX + JSON)
 */
@Validated
@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/bookclubs")
public class BookClubManageController {

    private final BookClubService bookClubService;

    @org.springframework.beans.factory.annotation.Value("${api.kakao.map.js-key}")
    private String kakaoJsKey;

    /**
     * 독서모임 관리 페이지 (모임장 전용)
     * GET /bookclubs/{bookClubId}/manage
     * - 모임 정보 조회
     * - JOINED 멤버 목록 조회
     * - WAIT 상태 가입 신청 목록 조회
     *
     * [권한 가드]
     * - 비로그인: /login으로 redirect
     * - 로그인했지만 모임장 아님: /bookclubs/{bookClubId}로 redirect + flash errorMessage
     * - 모임 없음: /bookclubs로 redirect + flash errorMessage
     */
    @GetMapping("/{bookClubId}/manage")
    public String getBookClubManagePage(
            @PathVariable("bookClubId") Long bookClubId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // 1. 로그인 확인
        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        if (loginMember == null) {
            log.warn("비로그인 상태에서 관리 페이지 접근 시도: bookClubId={}", bookClubId);
            return LoginUtil.redirectToLogin();
        }

        Long loginMemberSeq = loginMember.getMember_seq();

        // 2. 모임 조회
        BookClubVO bookClub = bookClubService.getBookClubById(bookClubId);
        if (bookClub == null || bookClub.getBook_club_deleted_dt() != null) {
            log.warn("존재하지 않는 모임 관리 페이지 접근: bookClubId={}, memberSeq={}", bookClubId, loginMemberSeq);
            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않거나 삭제된 모임입니다.");
            return "redirect:/bookclubs";
        }

        // 3. 모임장 권한 확인
        boolean isLeader = bookClub.getBook_club_leader_seq().equals(loginMemberSeq);
        if (!isLeader) {
            log.warn("모임장 아닌 사용자가 관리 페이지 접근: bookClubId={}, memberSeq={}, leaderSeq={}",
                    bookClubId, loginMemberSeq, bookClub.getBook_club_leader_seq());
            redirectAttributes.addFlashAttribute("errorMessage", "모임장만 접근할 수 있는 페이지입니다.");
            return "redirect:/bookclubs/" + bookClubId;
        }

        // 4. 관리 페이지 데이터 조회
        // 4-1. JOINED 멤버 목록 (N+1 방지: member_info 조인)
        var members = bookClubService.getJoinedMembersForManage(bookClubId);

        // 4-2. WAIT 상태 가입 신청 목록 (N+1 방지: member_info 조인)
        var pendingRequests = bookClubService.getPendingRequestsForManage(bookClubId);

        // 4-3. 현재 인원 집계
        int currentMemberCount = bookClubService.getTotalJoinedMemberCount(bookClubId);

        // 5. Model에 데이터 담기 (JSP에서 사용하는 키 이름과 정확히 일치)
        // bookclub_manage.jsp 참조 키: bookclub, members, pendingRequests
        model.addAttribute("bookclub", new BookClubManageViewDTO(bookClub, currentMemberCount));
        model.addAttribute("members", members);
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("kakaoJsKey", kakaoJsKey);

        log.info("관리 페이지 로드: bookClubId={}, memberCount={}, pendingCount={}",
                bookClubId, members.size(), pendingRequests.size());

        return "bookclub/bookclub_manage";
    }

    /**
     * 독서모임 관리 페이지 (레거시 URL 호환)
     * GET /bookclubs/{bookClubId}/edit -> redirect to /manage
     * - 기존 /edit URL 호환성 유지
     * - /manage로 redirect만 수행 (중복 로직 방지)
     */
    @GetMapping("/{bookClubId}/edit")
    public String redirectToManage(@PathVariable("bookClubId") Long bookClubId) {
        return "redirect:/bookclubs/" + bookClubId + "/manage";
    }

    /**
     * 독서모임 가입 신청 승인 (모임장 전용)
     * POST /bookclubs/{bookClubId}/manage/requests/{requestSeq}/approve
     *
     * 검증 순서:
     * 1. 로그인 확인
     * 2. 모임 존재 여부 확인
     * 3. 모임장 권한 확인
     * 4. Service 레이어에서 비즈니스 로직 처리:
     * - request WAIT 상태 검증
     * - 정원 초과 방지
     * - 중복 승인 방지
     * - book_club_member INSERT
     * - book_club_request UPDATE
     *
     * @return JSON {success, message, memberCount, pendingCount}
     */
    @PostMapping("/{bookClubId}/manage/requests/{requestSeq}/approve")
    @ResponseBody
    public Map<String, Object> approveJoinRequest(
            @PathVariable("bookClubId") Long bookClubId,
            @PathVariable("requestSeq") Long requestSeq,
            HttpSession session) {

        // 1. 로그인 확인
        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        if (loginMember == null) {
            log.warn("비로그인 상태에서 승인 시도: bookClubId={}, requestSeq={}", bookClubId, requestSeq);
            return Map.of("success", false, "message", "로그인이 필요합니다.");
        }

        Long loginMemberSeq = loginMember.getMember_seq();

        // 2. 모임 조회
        BookClubVO bookClub = bookClubService.getBookClubById(bookClubId);
        if (bookClub == null || bookClub.getBook_club_deleted_dt() != null) {
            log.warn("종료된 모임 승인 시도: bookClubId={}, requestSeq={}", bookClubId, requestSeq);
            return Map.of("success", false, "message", "존재하지 않거나 종료된 모임입니다.");
        }

        // 3. 모임장 권한 확인
        boolean isLeader = bookClub.getBook_club_leader_seq().equals(loginMemberSeq);
        if (!isLeader) {
            log.warn("모임장 아닌 사용자가 승인 시도: bookClubId={}, requestSeq={}, memberSeq={}, leaderSeq={}",
                    bookClubId, requestSeq, loginMemberSeq, bookClub.getBook_club_leader_seq());
            return Map.of("success", false, "message", "모임장만 승인할 수 있습니다.");
        }

        // 4. Service 호출 (비즈니스 로직 위임)
        try {
            bookClubService.approveJoinRequest(bookClubId, requestSeq, loginMemberSeq);

            // 5. 성공 시 현재 인원수와 대기 중인 신청 수 조회
            int memberCount = bookClubService.getTotalJoinedMemberCount(bookClubId);
            int pendingCount = bookClubService.getPendingRequestsForManage(bookClubId).size();

            log.info("가입 신청 승인 완료: bookClubId={}, requestSeq={}, memberCount={}, pendingCount={}",
                    bookClubId, requestSeq, memberCount, pendingCount);

            return Map.of(
                    "success", true,
                    "message", "가입 신청을 승인했습니다.",
                    "memberCount", memberCount,
                    "pendingCount", pendingCount);

        } catch (ClientException e) {
            log.warn("가입 신청 승인 실패: bookClubId={}, requestSeq={}, error={}",
                    bookClubId, requestSeq, e.getMessage());
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    /**
     * 독서모임 가입 신청 거절 (모임장 전용)
     * POST /bookclubs/{bookClubId}/manage/requests/{requestSeq}/reject
     *
     * 검증 순서:
     * 1. 로그인 확인
     * 2. 모임 존재 여부 확인
     * 3. 모임장 권한 확인
     * 4. Service 레이어에서 비즈니스 로직 처리:
     * - request WAIT 상태 검증
     * - book_club_request UPDATE (request_st='REJECTED')
     *
     * @return JSON {success, message, pendingCount}
     */
    @PostMapping("/{bookClubId}/manage/requests/{requestSeq}/reject")
    @ResponseBody
    public Map<String, Object> rejectJoinRequest(
            @PathVariable("bookClubId") Long bookClubId,
            @PathVariable("requestSeq") Long requestSeq,
            HttpSession session) {

        // 1. 로그인 확인
        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        if (loginMember == null) {
            log.warn("비로그인 상태에서 거절 시도: bookClubId={}, requestSeq={}", bookClubId, requestSeq);
            return Map.of("success", false, "message", "로그인이 필요합니다.");
        }

        Long loginMemberSeq = loginMember.getMember_seq();

        // 2. 모임 조회
        BookClubVO bookClub = bookClubService.getBookClubById(bookClubId);
        if (bookClub == null || bookClub.getBook_club_deleted_dt() != null) {
            log.warn("종료된 모임 거절 시도: bookClubId={}, requestSeq={}", bookClubId, requestSeq);
            return Map.of("success", false, "message", "존재하지 않거나 종료된 모임입니다.");
        }

        // 3. 모임장 권한 확인
        boolean isLeader = bookClub.getBook_club_leader_seq().equals(loginMemberSeq);
        if (!isLeader) {
            log.warn("모임장 아닌 사용자가 거절 시도: bookClubId={}, requestSeq={}, memberSeq={}, leaderSeq={}",
                    bookClubId, requestSeq, loginMemberSeq, bookClub.getBook_club_leader_seq());
            return Map.of("success", false, "message", "모임장만 거절할 수 있습니다.");
        }

        // 4. Service 호출 (비즈니스 로직 위임)
        try {
            bookClubService.rejectJoinRequest(bookClubId, requestSeq, loginMemberSeq);

            // 5. 성공 시 대기 중인 신청 수 조회
            int pendingCount = bookClubService.getPendingRequestsForManage(bookClubId).size();

            log.info("가입 신청 거절 완료: bookClubId={}, requestSeq={}, pendingCount={}",
                    bookClubId, requestSeq, pendingCount);

            return Map.of(
                    "success", true,
                    "message", "가입 신청을 거절했습니다.",
                    "pendingCount", pendingCount);

        } catch (ClientException e) {
            log.warn("가입 신청 거절 실패: bookClubId={}, requestSeq={}, error={}",
                    bookClubId, requestSeq, e.getMessage());
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    /**
     * 독서모임 멤버 강퇴 (모임장 전용)
     * POST /bookclubs/{bookClubId}/manage/members/{memberSeq}/kick
     *
     * 검증 순서:
     * 1. 로그인 확인
     * 2. 모임 존재 여부 확인
     * 3. 모임장 권한 확인
     * 4. Service 레이어에서 비즈니스 로직 처리:
     * - 타겟 멤버 JOINED 상태 검증
     * - 모임장 강퇴 방지
     * - book_club_member UPDATE (join_st='KICKED')
     *
     * @return JSON {success, message, memberCount}
     */
    @PostMapping("/{bookClubId}/manage/members/{memberSeq}/kick")
    @ResponseBody
    public Map<String, Object> kickMember(
            @PathVariable("bookClubId") Long bookClubId,
            @PathVariable("memberSeq") Long memberSeq,
            HttpSession session) {

        // 1. 로그인 확인
        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        if (loginMember == null) {
            log.warn("비로그인 상태에서 강퇴 시도: bookClubId={}, memberSeq={}", bookClubId, memberSeq);
            return Map.of("success", false, "message", "로그인이 필요합니다.");
        }

        Long loginMemberSeq = loginMember.getMember_seq();

        // 2. 모임 조회
        BookClubVO bookClub = bookClubService.getBookClubById(bookClubId);
        if (bookClub == null || bookClub.getBook_club_deleted_dt() != null) {
            log.warn("종료된 모임 강퇴 시도: bookClubId={}, memberSeq={}", bookClubId, memberSeq);
            return Map.of("success", false, "message", "존재하지 않거나 종료된 모임입니다.");
        }

        // 3. 모임장 권한 확인
        boolean isLeader = bookClub.getBook_club_leader_seq().equals(loginMemberSeq);
        if (!isLeader) {
            log.warn("모임장 아닌 사용자가 강퇴 시도: bookClubId={}, memberSeq={}, loginSeq={}, leaderSeq={}",
                    bookClubId, memberSeq, loginMemberSeq, bookClub.getBook_club_leader_seq());
            return Map.of("success", false, "message", "모임장만 강퇴할 수 있습니다.");
        }

        // 4. Service 호출 (비즈니스 로직 위임)
        try {
            bookClubService.kickMember(bookClubId, loginMemberSeq, memberSeq);

            // 5. 성공 시 현재 인원수 조회
            int memberCount = bookClubService.getTotalJoinedMemberCount(bookClubId);

            log.info("멤버 강퇴 완료: bookClubId={}, memberSeq={}, memberCount={}",
                    bookClubId, memberSeq, memberCount);

            return Map.of(
                    "success", true,
                    "message", "멤버를 퇴장시켰습니다.",
                    "memberCount", memberCount);

        } catch (ClientException e) {
            log.warn("멤버 강퇴 실패: bookClubId={}, memberSeq={}, error={}",
                    bookClubId, memberSeq, e.getMessage());
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    /**
     * 독서모임 설정 업데이트 (모임장 전용)
     * POST /bookclubs/{bookClubId}/manage/settings
     *
     * 검증 순서:
     * 1. 로그인 확인
     * 2. 모임 존재 여부 확인
     * 3. 모임장 권한 확인
     * 4. 파일 업로드 처리 (있는 경우)
     * 5. Service 레이어에서 비즈니스 로직 처리:
     * - 필수 입력값 검증 (name, description)
     * - 모임명 변경 시 중복 체크
     * - book_club UPDATE (정원 제외)
     *
     * @param bookClubId   독서모임 ID
     * @param name         모임 이름
     * @param description  모임 소개
     * @param region       지역
     * @param schedule     정기 일정
     * @param bannerFile   업로드 파일 (선택)
     * @param bannerImgUrl 이미지 URL (선택, 파일이 없을 때만 사용)
     * @param session      세션 (로그인 확인용)
     * @return JSON {success, message, updated}
     */
    @PostMapping("/{bookClubId}/manage/settings")
    @ResponseBody
    public Map<String, Object> updateBookClubSettings(
            @PathVariable("bookClubId") Long bookClubId,
            @NotBlank(message = "모임 이름을 입력해주세요.")
            @Size(max = 50, message = "모임 이름은 50자 이내로 입력해주세요.")
            @RequestParam("name") String name,
            @NotBlank(message = "모임 소개를 입력해주세요.")
            @Size(max = 500, message = "모임 소개는 500자 이내로 입력해주세요.")
            @RequestParam("description") String description,
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "schedule", required = false) String schedule,
            @RequestParam(value = "bannerFile", required = false) MultipartFile bannerFile,
            @RequestParam(value = "bannerImgUrl", required = false) String bannerImgUrl,
            HttpSession session) {

        // 1. 로그인 확인
        MemberVO loginMember = (MemberVO) session.getAttribute("loginSess");
        if (loginMember == null) {
            log.warn("비로그인 상태에서 설정 업데이트 시도: bookClubId={}", bookClubId);
            return Map.of("success", false, "message", "로그인이 필요합니다.");
        }

        Long loginMemberSeq = loginMember.getMember_seq();

        // 2. 모임 조회
        BookClubVO bookClub = bookClubService.getBookClubById(bookClubId);
        if (bookClub == null || bookClub.getBook_club_deleted_dt() != null) {
            log.warn("종료된 모임 설정 업데이트 시도: bookClubId={}", bookClubId);
            return Map.of("success", false, "message", "존재하지 않거나 종료된 모임입니다.");
        }

        // 3. 모임장 권한 확인
        boolean isLeader = bookClub.getBook_club_leader_seq().equals(loginMemberSeq);
        if (!isLeader) {
            log.warn("모임장 아닌 사용자가 설정 업데이트 시도: bookClubId={}, memberSeq={}, leaderSeq={}",
                    bookClubId, loginMemberSeq, bookClub.getBook_club_leader_seq());
            return Map.of("success", false, "message", "모임장만 수정할 수 있습니다.");
        }

        // 4. 파일 업로드 처리 (파일이 있으면 파일 우선) - 디버그 로그 추가
        log.info("배너 파일 업로드 체크: null={}, empty={}, originalFilename={}, size={}",
                bannerFile == null,
                bannerFile != null ? bannerFile.isEmpty() : "N/A",
                bannerFile != null ? bannerFile.getOriginalFilename() : "N/A",
                bannerFile != null ? bannerFile.getSize() : "N/A");

        String finalBannerUrl = bannerImgUrl;
        if (bannerFile != null && !bannerFile.isEmpty()) {
            try {
                finalBannerUrl = saveUploadedFile(bannerFile); // S3 URL 직접 저장
                log.info("배너 이미지 S3 업로드 완료: bookClubId={}, url={}", bookClubId, finalBannerUrl);
            } catch (ClientException e) {
                log.error("S3 업로드 실패: bookClubId={}, error={}", bookClubId, e.getMessage());
                return Map.of("success", false, "message", "파일 업로드에 실패했습니다.");
            }
        }

        // 5. DTO 생성
        BookClubUpdateSettingsDTO dto = new BookClubUpdateSettingsDTO();
        dto.setName(name);
        dto.setDescription(description);
        dto.setRegion(region);
        dto.setSchedule(schedule);
        dto.setBannerImgUrl(finalBannerUrl);

        // 6. Service 호출 (비즈니스 로직 위임 + afterCommit S3 삭제는 Service에서 처리)
        try {
            Map<String, Object> updated = bookClubService.updateBookClubSettings(bookClubId, loginMemberSeq, dto);

            log.info("모임 설정 업데이트 완료: bookClubId={}, newName={}", bookClubId, updated.get("name"));

            return Map.of(
                    "success", true,
                    "message", "저장되었습니다.",
                    "updated", updated);

        } catch (ClientException e) {
            log.warn("모임 설정 업데이트 실패: bookClubId={}, error={}", bookClubId, e.getMessage());
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public Map<String, Object> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(cv -> cv.getMessage())
                .findFirst()
                .orElse("입력값을 확인해주세요.");
        return Map.of("success", false, "message", message);
    }

    /**
     * 업로드된 파일을 S3에 저장
     *
     * @param file 업로드 파일
     * @return
     *
     * @throws IOException 파일 저장 실패 시
     */
    private String saveUploadedFile(MultipartFile file) {
        return bookClubService.uploadFile(file);
    }
}
