package project.bookclub;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.bookclub.ENUM.JoinRequestResult;
import project.bookclub.ENUM.JoinStatus;
import project.bookclub.ENUM.RequestStatus;
import project.bookclub.dto.BookClubPageResponseDTO;
import project.bookclub.vo.BookClubBoardVO;
import project.bookclub.vo.BookClubVO;
import project.util.exception.bookclub.BookClubInvalidRequestException;
import project.util.exception.file.FileUploadException;
import project.util.imgUpload.ImgService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BookClubService {

    private final BookClubMapper bookClubMapper;
    private final ImgService imgService; // @Primary → FileUploadService

    private static final int DEFAULT_PAGE_SIZE = 20; // 5개 x 4줄

    // 이미지 업로드 검증 상수
    private static final java.util.Set<String> ALLOWED_IMAGE_EXTENSIONS = java.util.Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final java.util.Set<String> ALLOWED_MIME_TYPES = java.util.Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /*
     * #0. 파일 업로드 (컨트롤러에서 이동)
     */
    /**
     * 이미지 파일 S3 업로드
     * 이미지 파일만 허용 (jpg, jpeg, png, gif, webp)
     * @return S3 전체 URL (예: https://secondarybooksimages.s3.ap-northeast-2.amazonaws.com/images/{UUID}.jpg)
     * @throws java.io.IOException 업로드 실패 시
     */
    public String uploadFile(org.springframework.web.multipart.MultipartFile file) {
        // 1. 파일명 유효성 검사
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new FileUploadException("파일명이 유효하지 않습니다.");
        }
        if (!originalFilename.contains(".")) {
            throw new FileUploadException("확장자가 없는 파일은 업로드할 수 없습니다.");
        }

        // 2. 확장자 화이트리스트 검증
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new FileUploadException("허용되지 않는 파일 형식입니다.");
        }

        // 3. MIME 타입 검증
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new FileUploadException("파일 형식을 확인할 수 없습니다.");
        }
        if (!ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new FileUploadException("허용되지 않는 파일 형식입니다.");
        }

        // 4. 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException("파일 크기는 5MB를 초과할 수 없습니다.");
        }

        // 5. 이미지 업로드 (ImgService 구현체에 위임)
        try {
            return imgService.uploadFile(file);
        } catch (Exception e) {
            throw new FileUploadException("이미지 업로드 실패: " + e.getMessage(), e);
        }
    }


    // #1-1. 전체 독서모임 리스트 조회 (최신순 정렬, 첫 페이지)
    @Cacheable(value = "bookClubList", key = "'latest:first:' + (#p0 != null ? #p0 : 'guest')")
    public List<BookClubVO> getBookClubList(Long memberSeq) {
        return bookClubMapper.searchAllWithSort("latest", DEFAULT_PAGE_SIZE, 0, memberSeq);
    }

    // #1-2. 독서모임 검색 (정렬 + 페이징)
    public BookClubPageResponseDTO searchBookClubs(String keyword, String sort, int page, Long memberSeq) {
        // 정렬 옵션 검증 (기본값: latest)
        if (sort == null || (!sort.equals("latest") && !sort.equals("activity"))) {
            sort = "latest";
        }

        if (page < 0) {
            page = 0;
        }

        int offset = page * DEFAULT_PAGE_SIZE;
        List<BookClubVO> content;
        long totalElements;

        // 키워드 없으면 전체 검색
        if (keyword == null || keyword.isBlank()) {
            content = bookClubMapper.searchAllWithSort(sort, DEFAULT_PAGE_SIZE, offset, memberSeq);
            totalElements = bookClubMapper.countAll();
        } else {
            List<String> tokens = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(keyword);
            while (st.hasMoreTokens()) {
                tokens.add(st.nextToken());
            }

            if (tokens.isEmpty()) {
                content = bookClubMapper.searchAllWithSort(sort, DEFAULT_PAGE_SIZE, offset, memberSeq);
                totalElements = bookClubMapper.countAll();
            } else {
                content = bookClubMapper.searchByKeywordWithSort(tokens, sort, DEFAULT_PAGE_SIZE, offset, memberSeq);
                totalElements = bookClubMapper.countByKeyword(tokens);
            }
        }

        int totalPages = (int) Math.ceil((double) totalElements / DEFAULT_PAGE_SIZE);

        return BookClubPageResponseDTO.builder()
                .content(content)
                .page(page)
                .size(DEFAULT_PAGE_SIZE)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1 || totalPages == 0)
                .build();
    }


    /*
     * #2. 독서모임 상세 페이지
     */
    // #2-1. 독서모임 1건 조회 (상세 페이지)
    @Cacheable(value = "bookClub", key = "#bookClubSeq", unless = "#result ==null")
    public BookClubVO getBookClubById(Long bookClubSeq) {
        return bookClubMapper.selectById(bookClubSeq);
    }

    // #2-2. 특정 멤버가 JOINED 상태로 가입되어 있는지 확인
    public boolean isMemberJoined(Long bookClubSeq, Long memberSeq) {
        if (bookClubSeq == null || memberSeq == null) {
            return false;
        }
        int count = bookClubMapper.selectJoinedMemberCount(bookClubSeq, memberSeq);
        return count > 0;
    }

    // #2-2-1. 특정 멤버가 모임장인지 확인
    public boolean isLeader(Long bookClubSeq, Long memberSeq) {
        if (bookClubSeq == null || memberSeq == null) {
            return false;
        }
        BookClubVO bookClub = bookClubMapper.selectById(bookClubSeq);
        if (bookClub == null) {
            return false;
        }
        return memberSeq.equals(bookClub.getBook_club_leader_seq());
    }

    // #2-3. 독서모임의 전체 JOINED 멤버 수 조회
    public int getTotalJoinedMemberCount(Long bookClubSeq) {
        if (bookClubSeq == null) {
            return 0;
        }
        return bookClubMapper.getTotalJoinedMemberCount(bookClubSeq);
    }

    // #2-4. 특정 멤버가 대기중인 가입 신청이 있는지 확인
    public boolean hasPendingRequest(Long bookClubSeq, Long memberSeq) {
        if (bookClubSeq == null || memberSeq == null) {
            return false;
        }
        int count = bookClubMapper.selectPendingRequestCount(bookClubSeq, memberSeq);
        return count > 0;
    }

    // #2-4-2. 특정 멤버가 거절된 가입 신청이 있는지 확인
    public boolean hasRejectedRequest(Long bookClubSeq, Long memberSeq) {
        if (bookClubSeq == null || memberSeq == null) {
            return false;
        }
        int count = bookClubMapper.selectRejectedRequestCount(bookClubSeq, memberSeq);
        return count > 0;
    }

    // #2-5. 가입 신청 생성 (개선판: 예외를 enum 결과로 변환)
    /**
     * 독서모임 가입 신청 생성
     *
     * 검증 순서:
     * 1. 파라미터 null 체크 → INVALID_PARAMETERS
     * 2. book_club_member.join_st='JOINED' 확인 → ALREADY_JOINED
     * 3. book_club_request.request_st='WAIT' 확인 → ALREADY_REQUESTED
     * 4. book_club_request INSERT → SUCCESS
     *
     * 참고:
     * - book_club_request 테이블: UNIQUE(book_club_seq, request_member_seq) 제약 존재
     * (uq_bcr_wait)
     * - DB 레벨에서 중복 신청 방지 (동일 멤버가 동일 모임에 여러 request 불가)
     * - 비즈니스 로직(hasPendingRequest)으로 1차 검증, DB UNIQUE 제약으로 2차 방어
     *
     * @param bookClubSeq 독서모임 ID
     * @param memberSeq   신청 멤버 ID
     * @param requestCont 신청 메시지 (nullable)
     * @return JoinRequestResult enum (성공/실패 사유)
     */
    @Transactional
    public JoinRequestResult createJoinRequest(Long bookClubSeq, Long memberSeq, String requestCont) {
        // 1. 파라미터 검증
        if (bookClubSeq == null || memberSeq == null) {
            log.warn("가입 신청 실패: 잘못된 파라미터 - bookClubSeq={}, memberSeq={}", bookClubSeq, memberSeq);
            return JoinRequestResult.INVALID_PARAMETERS;
        }

        // 2. 이미 JOINED 상태인지 확인 (book_club_member 테이블)
        // - UNIQUE 제약: uq_bcm_bookclub_member (book_club_seq, member_seq)
        if (isMemberJoined(bookClubSeq, memberSeq)) {
            log.info("가입 신청 실패: 이미 가입된 멤버 (join_st=JOINED) - bookClubSeq={}, memberSeq={}",
                    bookClubSeq, memberSeq);
            return JoinRequestResult.ALREADY_JOINED;
        }

        // 3. 이미 신청 이력이 있는지 확인 (book_club_request 테이블)
        // - UNIQUE 제약: uq_bcr_wait (book_club_seq, request_member_seq)
        // - WAIT, APPROVED, REJECTED 상관없이 동일 멤버는 1개의 request만 가질 수 있음
        if (hasPendingRequest(bookClubSeq, memberSeq)) {
            log.info("가입 신청 실패: 이미 신청 대기중 (request_st=WAIT) - bookClubSeq={}, memberSeq={}",
                    bookClubSeq, memberSeq);
            return JoinRequestResult.ALREADY_REQUESTED;
        }

        // 4. INSERT 시도
        try {
            bookClubMapper.insertJoinRequest(bookClubSeq, memberSeq, requestCont);
            log.info("가입 신청 성공: bookClubSeq={}, memberSeq={}", bookClubSeq, memberSeq);
            return JoinRequestResult.SUCCESS;

        } catch (DataIntegrityViolationException e) {
            // UNIQUE 제약 위반 시 (동시성 이슈로 SELECT 이후 INSERT 전에 다른 트랜잭션이 먼저 INSERT한 경우)
            // uq_bcr_wait: UNIQUE(book_club_seq, request_member_seq)
            log.warn("가입 신청 실패: DB UNIQUE 제약 위반 - bookClubSeq={}, memberSeq={}, error={}",
                    bookClubSeq, memberSeq, e.getMessage());
            return JoinRequestResult.ALREADY_REQUESTED;
        }
    }

    // #2-6. 독서모임의 찜 개수 조회
    public int getWishCount(Long bookClubSeq) {
        if (bookClubSeq == null) {
            return 0;
        }
        return bookClubMapper.selectWishCount(bookClubSeq);
    }

    // #2-7. 특정 멤버의 찜 여부 확인
    public boolean isWished(Long bookClubSeq, Long memberSeq) {
        if (bookClubSeq == null || memberSeq == null) {
            return false;
        }
        return bookClubMapper.selectWishExists(bookClubSeq, memberSeq) > 0;
    }

    // #2-8. 찜 토글 (찜 추가/취소)
    @Transactional
    @CacheEvict(value = "bookClubList", key = "'latest:first:' + (#p1 != null ? #p1 : 'guest')")
    public boolean toggleWish(Long bookClubSeq, Long memberSeq) {
        if (bookClubSeq == null || memberSeq == null) {
            return false;
        }

        boolean currentlyWished = isWished(bookClubSeq, memberSeq);

        if (currentlyWished) {
            // 이미 찜한 상태 → 찜 취소
            bookClubMapper.deleteWish(bookClubSeq, memberSeq);
            return false; // 찜 해제됨
        } else {
            // 찜 안한 상태 → 찜 추가
            bookClubMapper.insertWish(bookClubSeq, memberSeq);
            return true; // 찜됨
        }
    }

    /*
     * #3. 독서모임 게시판
     */
    // #3-1. 독서모임 게시판 - 최근 원글 10개 조회
    public List<BookClubBoardVO> getRecentBoards(Long bookClubSeq) {
        if (bookClubSeq == null) {
            return List.of();
        }
        return bookClubMapper.selectRecentRootBoardsByClub(bookClubSeq);
    }

    // #3-2. 독서모임 게시글 단건 조회 (상세 페이지)
    public BookClubBoardVO getBoardDetail(Long bookClubSeq, Long postId) {
        if (bookClubSeq == null || postId == null) {
            return null;
        }
        return bookClubMapper.selectBoardDetail(bookClubSeq, postId);
    }

    // #3-3. 독서모임 게시글의 댓글 목록 조회
    public List<BookClubBoardVO> getBoardComments(Long bookClubSeq, Long postId) {
        if (bookClubSeq == null || postId == null) {
            return List.of();
        }
        return bookClubMapper.selectBoardComments(bookClubSeq, postId);
    }

    // #3-4. 부모글(원글) 존재 여부 확인 (우회 방지용)
    public boolean existsRootPost(Long bookClubSeq, Long postId) {
        if (bookClubSeq == null || postId == null) {
            return false;
        }
        return bookClubMapper.existsRootPost(bookClubSeq, postId) > 0;
    }

    // #3-5. 댓글 작성
    @Transactional
    public int createBoardComment(Long bookClubSeq, Long postId, Long memberSeq, String commentCont) {
        return bookClubMapper.insertBoardComment(bookClubSeq, postId, memberSeq, commentCont);
    }

    // #3-5-1. 댓글 단건 조회 (권한 확인용)
    public BookClubBoardVO getBoardCommentById(Long commentId) {
        if (commentId == null) {
            return null;
        }
        return bookClubMapper.selectBoardCommentById(commentId);
    }

    // #3-5-2. 댓글 수정
    @Transactional
    public boolean updateBoardComment(Long commentId, String commentCont) {
        if (commentId == null || commentCont == null || commentCont.isBlank()) {
            return false;
        }
        int result = bookClubMapper.updateBoardComment(commentId, commentCont);
        return result > 0;
    }

    // #3-5-3. 댓글 삭제 (soft delete)
    @Transactional
    public boolean deleteBoardComment(Long commentId) {
        if (commentId == null) {
            return false;
        }
        int result = bookClubMapper.deleteBoardComment(commentId);
        return result > 0;
    }

    // #3-6. 게시글(원글) 작성
    @Transactional
    public Long createBoardPost(BookClubBoardVO boardVO) {
        bookClubMapper.insertBoardPost(boardVO);
        return boardVO.getBook_club_board_seq();
    }

    // #3-7. 게시글(원글) 수정
    @Transactional
    public boolean updateBoardPost(BookClubBoardVO boardVO, String oldImageUrl) {
        // 1. 게시글 UPDATE
        int result = bookClubMapper.updateBoardPost(boardVO);

        // 2. 이미지가 교체/삭제되었으면 기존 S3 이미지 삭제 예약 (afterCommit)
        String newImageUrl = boardVO.getBoard_img_url();
        if (result > 0 && oldImageUrl != null && !oldImageUrl.equals(newImageUrl)) {
            scheduleS3DeletionAfterCommit(oldImageUrl);
        }

        return result > 0;
    }

    // #3-8. 게시글(원글) 삭제 (soft delete)
    @Transactional
    public boolean deleteBoardPost(Long bookClubSeq, Long postId) {
        if (bookClubSeq == null || postId == null) {
            return false;
        }

        // 1. 삭제 전에 이미지 URL 조회 (afterCommit 삭제용)
        BookClubBoardVO boardVO = bookClubMapper.selectBoardDetail(bookClubSeq, postId);
        String oldImageUrl = (boardVO != null) ? boardVO.getBoard_img_url() : null;

        // 2. 게시글 소프트 딜리트 (board_deleted_dtm=CURRENT_TIMESTAMP)
        int result = bookClubMapper.deleteBoardPost(bookClubSeq, postId);

        // 3. 삭제 성공 시 S3 이미지 삭제 예약 (afterCommit)
        if (result > 0 && oldImageUrl != null && !oldImageUrl.isEmpty()) {
            scheduleS3DeletionAfterCommit(oldImageUrl);
        }

        return result > 0;
    }

    /*
     * #4. 독서모임 생성
     */
    @Transactional
    @CacheEvict(value = "bookClubList", allEntries = true)
    public void createBookClub(BookClubVO vo) {
        // 1. 모임 이름 중복 체크 (리더별)
        boolean duplicated = isDuplicateForLeader(vo.getBook_club_leader_seq(), vo.getBook_club_name());
        if (duplicated) {
            throw new BookClubInvalidRequestException("이미 같은 이름의 독서 모임이 존재합니다.");
        }

        // 3-1. 배너 이미지가 설정되지 않았으면 랜덤으로 기본 이미지 선택 (10개 중 1개)
        if (vo.getBanner_img_url() == null || vo.getBanner_img_url().trim().isEmpty()) {
            int randomNum = (int) (Math.random() * 10) + 1;
            String defaultBanner = "/resources/img/bookclub/banners/bookclub"
                    + (randomNum == 1 ? "" : String.valueOf(randomNum)) + ".png";
            vo.setBanner_img_url(defaultBanner);
            log.info("기본 배너 이미지 설정: {}", defaultBanner);
        }

        // 4. DB 저장 (book_club 테이블)
        bookClubMapper.insertBookClub(vo);

        // 5. 생성된 book_club_seq를 이용해 모임장을 book_club_member에 자동 등록
        // - join_st='JOINED', leader_yn=true
        // - 관리 페이지의 멤버 목록에 모임장이 나타나게 함
        Long bookClubSeq = vo.getBook_club_seq();
        Long leaderSeq = vo.getBook_club_leader_seq();

        if (bookClubSeq != null && leaderSeq != null) {
            bookClubMapper.insertLeaderMember(bookClubSeq, leaderSeq);
            log.info("모임장을 book_club_member에 등록 완료: bookClubSeq={}, leaderSeq={}", bookClubSeq, leaderSeq);
        } else {
            log.warn("모임장 자동 등록 실패: bookClubSeq 또는 leaderSeq가 null - bookClubSeq={}, leaderSeq={}", bookClubSeq,
                    leaderSeq);
        }
    }

    public boolean isDuplicateForLeader(Long leaderSeq, String name) {
        return bookClubMapper.countByLeaderAndName(leaderSeq, name) > 0;
    }

    public List<BookClubVO> getMyBookClubs(long member_seq) {
        return bookClubMapper.selectMyBookClubs(member_seq);
    }

    public List<BookClubVO> getWishBookClubs(long member_seq) {
        return bookClubMapper.selectWishBookClubs(member_seq);
    }

    /*
     * #5. 독서모임 관리 페이지
     */
    // #5-1. 관리 페이지 - JOINED 멤버 목록 조회
    public List<project.bookclub.dto.BookClubManageMemberDTO> getJoinedMembersForManage(Long bookClubSeq) {
        if (bookClubSeq == null) {
            return List.of();
        }
        return bookClubMapper.selectJoinedMembersForManage(bookClubSeq);
    }

    // #5-2. 관리 페이지 - WAIT 상태 가입 신청 목록 조회
    public List<project.bookclub.dto.BookClubJoinRequestDTO> getPendingRequestsForManage(Long bookClubSeq) {
        if (bookClubSeq == null) {
            return List.of();
        }
        return bookClubMapper.selectPendingRequestsForManage(bookClubSeq);
    }

    // #5-3. 관리 페이지 - 가입 신청 승인
    /**
     * 독서모임 가입 신청 승인 (재가입 지원 + 동시성 제어)
     *
     * 검증 및 처리 순서:
     * 1. 파라미터 null 체크
     * 2. book_club row 비관적 락 획득 (FOR UPDATE) - 정원 초과 동시성 방지
     * 3. request 조회 및 WAIT 상태 검증
     * 4. 정원 초과 방지 (JOINED 멤버 수 < max_member) - 락 보호 하에 체크
     * 5. 멤버 상태 확인:
     * - 멤버 row 없음 → INSERT (신규 가입)
     * - join_st='JOINED' → 승인 실패 (이미 가입된 멤버)
     * - join_st='LEFT/KICKED/REJECTED/WAIT' → UPDATE (재가입 복구)
     * 6. book_club_request UPDATE (request_st='APPROVED', request_processed_dt=오늘)
     *
     * 동시성 제어:
     * - lockBookClubForUpdate()로 book_club row를 FOR UPDATE 잠금
     * - 트랜잭션 커밋 전까지 다른 승인 요청은 대기 → 정원 초과 방지
     *
     * @param bookClubSeq 독서모임 ID
     * @param requestSeq  가입 신청 ID
     * @param leaderSeq   모임장 ID (권한 체크는 Controller에서 이미 완료)
     * @throws IllegalArgumentException 파라미터가 null인 경우
     * @throws IllegalStateException    비즈니스 규칙 위반 시
     */
    @Transactional
    @CacheEvict(value = "bookClub", key = "#bookClubSeq")
    public void approveJoinRequest(Long bookClubSeq, Long requestSeq, Long leaderSeq) {
        // 1. 파라미터 검증
        if (bookClubSeq == null || requestSeq == null || leaderSeq == null) {
            log.warn("승인 처리 실패: 잘못된 파라미터 - bookClubSeq={}, requestSeq={}, leaderSeq={}",
                    bookClubSeq, requestSeq, leaderSeq);
            throw new BookClubInvalidRequestException("잘못된 요청입니다.");
        }

        // 2. book_club row 비관적 락 획득 (동시성 제어 - 정원 초과 방지)
        Long lockedClubSeq = bookClubMapper.lockBookClubForUpdate(bookClubSeq);
        if (lockedClubSeq == null) {
            log.warn("승인 처리 실패: 모임이 존재하지 않거나 삭제됨 - bookClubSeq={}", bookClubSeq);
            throw new BookClubInvalidRequestException("존재하지 않거나 종료된 모임입니다.");
        }

        // 3. request 조회 및 WAIT 상태 검증 (조건부 선점)
        project.bookclub.dto.BookClubJoinRequestDTO request = bookClubMapper.selectRequestById(requestSeq);
        if (request == null) {
            log.warn("승인 처리 실패: 존재하지 않는 신청 - requestSeq={}", requestSeq);
            throw new BookClubInvalidRequestException("존재하지 않는 가입 신청입니다.");
        }

        if (request.getRequestSt() != RequestStatus.WAIT) {
            log.warn("승인 처리 실패: WAIT 상태가 아님 - requestSeq={}, requestSt={}",
                    requestSeq, request.getRequestSt());
            throw new BookClubInvalidRequestException("이미 처리된 신청입니다.");
        }

        // bookClubSeq 일치 확인 (URL 파라미터 vs DB)
        if (!bookClubSeq.equals(request.getBookClubSeq())) {
            log.warn("승인 처리 실패: bookClubSeq 불일치 - URL={}, DB={}",
                    bookClubSeq, request.getBookClubSeq());
            throw new BookClubInvalidRequestException("잘못된 요청입니다.");
        }

        // 4. 정원 초과 방지 (락 획득 후 안전하게 체크)
        BookClubVO bookClub = bookClubMapper.selectById(bookClubSeq);
        if (bookClub == null) {
            log.warn("승인 처리 실패: 존재하지 않는 모임 - bookClubSeq={}", bookClubSeq);
            throw new BookClubInvalidRequestException("존재하지 않는 모임입니다.");
        }

        int currentMemberCount = bookClubMapper.getTotalJoinedMemberCount(bookClubSeq);
        int maxMember = bookClub.getBook_club_max_member();

        if (currentMemberCount >= maxMember) {
            log.warn("승인 처리 실패: 정원 초과 - bookClubSeq={}, currentCount={}, maxMember={}",
                    bookClubSeq, currentMemberCount, maxMember);
            throw new BookClubInvalidRequestException("모임 정원이 초과되었습니다.");
        }

        // 5. 멤버 상태 확인 (재가입 지원)
        Long memberSeq = request.getRequestMemberSeq();
        JoinStatus joinSt = bookClubMapper.selectMemberJoinSt(bookClubSeq, memberSeq);

        if (joinSt == null) {
            // 5-A. 멤버 row 없음 → 신규 가입 (INSERT)
            bookClubMapper.insertMember(bookClubSeq, memberSeq);
            log.info("신규 멤버 등록 완료: bookClubSeq={}, memberSeq={}", bookClubSeq, memberSeq);

        } else if (joinSt == JoinStatus.JOINED) {
            // 5-B. 이미 JOINED 상태 → 승인 실패
            log.warn("승인 처리 실패: 이미 JOINED 상태 - bookClubSeq={}, memberSeq={}, joinSt={}",
                    bookClubSeq, memberSeq, joinSt);
            throw new BookClubInvalidRequestException("이미 가입된 멤버입니다.");

        } else {
            // 5-C. LEFT/KICKED/REJECTED/WAIT 등 JOINED가 아닌 상태 → 재가입 복구 (UPDATE)
            int updatedRows = bookClubMapper.restoreMemberToJoined(bookClubSeq, memberSeq);
            if (updatedRows == 0) {
                log.warn("승인 처리 실패: 재가입 복구 UPDATE 실패 - bookClubSeq={}, memberSeq={}, joinSt={}",
                        bookClubSeq, memberSeq, joinSt);
                throw new BookClubInvalidRequestException("승인 처리에 실패했습니다.");
            }
            log.info("재가입 멤버 복구 완료: bookClubSeq={}, memberSeq={}, oldJoinSt={} -> JOINED",
                    bookClubSeq, memberSeq, joinSt);
        }

        // 6. book_club_request UPDATE (최종 처리)
        bookClubMapper.updateRequestStatus(requestSeq, RequestStatus.APPROVED);
        log.info("가입 신청 승인 완료: requestSeq={}", requestSeq);
    }

    // #5-4. 관리 페이지 - 가입 신청 거절
    /**
     * 독서모임 가입 신청 거절
     *
     * 검증 및 처리 순서:
     * 1. 파라미터 null 체크
     * 2. request 조회 및 WAIT 상태 검증
     * 3. book_club_request UPDATE (request_st='REJECTED', request_processed_dt=오늘)
     *
     * @param bookClubSeq 독서모임 ID
     * @param requestSeq  가입 신청 ID
     * @param leaderSeq   모임장 ID (권한 체크는 Controller에서 이미 완료)
     * @throws IllegalArgumentException 파라미터가 null인 경우
     * @throws IllegalStateException    비즈니스 규칙 위반 시
     */
    @Transactional
    public void rejectJoinRequest(Long bookClubSeq, Long requestSeq, Long leaderSeq) {
        // 1. 파라미터 검증
        if (bookClubSeq == null || requestSeq == null || leaderSeq == null) {
            log.warn("거절 처리 실패: 잘못된 파라미터 - bookClubSeq={}, requestSeq={}, leaderSeq={}",
                    bookClubSeq, requestSeq, leaderSeq);
            throw new BookClubInvalidRequestException("잘못된 요청입니다.");
        }

        // 2. request 조회 및 WAIT 상태 검증
        project.bookclub.dto.BookClubJoinRequestDTO request = bookClubMapper.selectRequestById(requestSeq);
        if (request == null) {
            log.warn("거절 처리 실패: 존재하지 않는 신청 - requestSeq={}", requestSeq);
            throw new BookClubInvalidRequestException("존재하지 않는 가입 신청입니다.");
        }

        if (request.getRequestSt() != RequestStatus.WAIT) {
            log.warn("거절 처리 실패: WAIT 상태가 아님 - requestSeq={}, requestSt={}",
                    requestSeq, request.getRequestSt());
            throw new BookClubInvalidRequestException("이미 처리된 신청입니다.");
        }

        // bookClubSeq 일치 확인 (URL 파라미터 vs DB)
        if (!bookClubSeq.equals(request.getBookClubSeq())) {
            log.warn("거절 처리 실패: bookClubSeq 불일치 - URL={}, DB={}",
                    bookClubSeq, request.getBookClubSeq());
            throw new BookClubInvalidRequestException("잘못된 요청입니다.");
        }

        // 3. book_club_request UPDATE
        bookClubMapper.updateRequestStatus(requestSeq, RequestStatus.REJECTED);
        log.info("가입 신청 거절 완료: requestSeq={}", requestSeq);
    }

    // #5-5. 관리 페이지 - 멤버 강퇴
    /**
     * 독서모임 멤버 강퇴
     *
     * 검증 및 처리 순서:
     * 1. 파라미터 null 체크
     * 2. 타겟 멤버 조회 및 JOINED 상태 검증
     * 3. 모임장 강퇴 방지 (leader_yn=true 체크)
     * 4. book_club_member UPDATE (join_st='KICKED',
     * join_st_update_dtm=CURRENT_TIMESTAMP)
     *
     * @param bookClubSeq     독서모임 ID
     * @param leaderSeq       모임장 ID (권한 체크는 Controller에서 이미 완료)
     * @param targetMemberSeq 강퇴 대상 멤버 ID
     * @throws IllegalArgumentException 파라미터가 null인 경우
     * @throws IllegalStateException    비즈니스 규칙 위반 시
     */
    @Transactional
    public void kickMember(Long bookClubSeq, Long leaderSeq, Long targetMemberSeq) {
        // 1. 파라미터 검증
        if (bookClubSeq == null || leaderSeq == null || targetMemberSeq == null) {
            log.warn("멤버 강퇴 실패: 잘못된 파라미터 - bookClubSeq={}, leaderSeq={}, targetMemberSeq={}",
                    bookClubSeq, leaderSeq, targetMemberSeq);
            throw new BookClubInvalidRequestException("잘못된 요청입니다.");
        }

        // 2. 타겟 멤버 조회 및 상태 검증
        project.bookclub.dto.BookClubManageMemberDTO member = bookClubMapper.selectMemberBySeq(bookClubSeq,
                targetMemberSeq);
        if (member == null) {
            log.warn("멤버 강퇴 실패: 존재하지 않는 멤버 - bookClubSeq={}, targetMemberSeq={}",
                    bookClubSeq, targetMemberSeq);
            throw new BookClubInvalidRequestException("존재하지 않는 멤버입니다.");
        }

        // 3. JOINED 상태 검증
        if (member.getJoinSt() != JoinStatus.JOINED) {
            log.warn("멤버 강퇴 실패: JOINED 상태가 아님 - bookClubSeq={}, targetMemberSeq={}, joinSt={}",
                    bookClubSeq, targetMemberSeq, member.getJoinSt());
            throw new BookClubInvalidRequestException("가입된 멤버가 아닙니다.");
        }

        // 4. 모임장 강퇴 방지 (leader_yn='Y')
        if ("Y".equals(member.getLeaderYn())) {
            log.warn("멤버 강퇴 실패: 모임장 강퇴 시도 - bookClubSeq={}, targetMemberSeq={}",
                    bookClubSeq, targetMemberSeq);
            throw new BookClubInvalidRequestException("모임장은 강퇴할 수 없습니다.");
        }

        // 5. book_club_member UPDATE (join_st='KICKED')
        int updatedRows = bookClubMapper.updateMemberToKicked(bookClubSeq, targetMemberSeq);
        if (updatedRows == 0) {
            log.warn("멤버 강퇴 실패: 업데이트 대상 없음 - bookClubSeq={}, targetMemberSeq={}",
                    bookClubSeq, targetMemberSeq);
            throw new BookClubInvalidRequestException("강퇴 처리에 실패했습니다.");
        }

        log.info("멤버 강퇴 완료: bookClubSeq={}, targetMemberSeq={}", bookClubSeq, targetMemberSeq);
    }

    // #5-6. 관리 페이지 - 모임 설정 업데이트 (정보 수정)
    /**
     * 독서모임 정보 수정
     *
     * 검증 및 처리 순서:
     * 1. 파라미터 null 체크
     * 2. 모임 존재 확인
     * 3. 모임장 권한 확인 (leaderSeq == book_club_leader_seq)
     * 4. 모임명 변경 시 중복 체크 (동일 리더의 다른 모임 중복 확인)
     * 5. book_club UPDATE (정원 제외)
     * 6. 업데이트 성공 확인 (rowCount=1)
     * 7. 최신 book_club 조회 후 필요한 필드 반환
     *
     * @param bookClubSeq 독서모임 ID
     * @param leaderSeq   모임장 ID (권한 체크용)
     * @param dto         업데이트할 정보 (name, desc, region, schedule, bannerImgUrl)
     * @return 업데이트된 모임 정보 (Map)
     * @throws IllegalArgumentException 파라미터가 null이거나 잘못된 경우
     * @throws IllegalStateException    비즈니스 규칙 위반 시
     */
    @Transactional
    @org.springframework.cache.annotation.Caching(evict = {
            @org.springframework.cache.annotation.CacheEvict(value = "bookClub", key = "#bookClubSeq"),
            @org.springframework.cache.annotation.CacheEvict(value = "bookClubList", allEntries = true)
    })
    public java.util.Map<String, Object> updateBookClubSettings(
            Long bookClubSeq,
            Long leaderSeq,
            project.bookclub.dto.BookClubUpdateSettingsDTO dto) {

        // 1. 파라미터 검증
        if (bookClubSeq == null || leaderSeq == null || dto == null) {
            log.warn("모임 설정 업데이트 실패: 잘못된 파라미터 - bookClubSeq={}, leaderSeq={}, dto={}",
                    bookClubSeq, leaderSeq, dto);
            throw new BookClubInvalidRequestException("잘못된 요청입니다.");
        }

        // 필수 입력값 검증 (trim 후 빈값 체크)
        String newName = trimToNull(dto.getName());
        String newDescription = trimToNull(dto.getDescription());

        if (newName == null) {
            throw new BookClubInvalidRequestException("모임 이름을 입력해주세요.");
        }
        if (newDescription == null) {
            throw new BookClubInvalidRequestException("모임 소개를 입력해주세요.");
        }

        // 2. 모임 존재 확인
        BookClubVO bookClub = bookClubMapper.selectById(bookClubSeq);
        if (bookClub == null) {
            log.warn("모임 설정 업데이트 실패: 존재하지 않는 모임 - bookClubSeq={}", bookClubSeq);
            throw new BookClubInvalidRequestException("존재하지 않는 모임입니다.");
        }

        // 2-1. 기존 배너 URL 확보 (S3 삭제용)
        String oldBannerUrl = bookClub.getBanner_img_url();

        // 3. 모임장 권한 확인
        if (!bookClub.getBook_club_leader_seq().equals(leaderSeq)) {
            log.warn("모임 설정 업데이트 실패: 모임장 아님 - bookClubSeq={}, leaderSeq={}, actualLeaderSeq={}",
                    bookClubSeq, leaderSeq, bookClub.getBook_club_leader_seq());
            throw new BookClubInvalidRequestException("모임장만 수정할 수 있습니다.");
        }

        // 4. 모임명 변경 시 중복 체크 (현재 모임 제외)
        String currentName = bookClub.getBook_club_name();

        if (!newName.equals(currentName)) {
            // 모임명이 변경된 경우 중복 확인
            int duplicateCount = bookClubMapper.countByLeaderAndNameExcludingSelf(leaderSeq, newName, bookClubSeq);
            if (duplicateCount > 0) {
                log.warn("모임 설정 업데이트 실패: 모임명 중복 - bookClubSeq={}, leaderSeq={}, newName={}",
                        bookClubSeq, leaderSeq, newName);
                throw new BookClubInvalidRequestException("이미 같은 이름의 모임이 존재합니다.");
            }
        }

        // 5. book_club UPDATE (정원은 제외)
        // 빈값 정책: 빈 문자열("")은 null로 변환하여 저장
        int updatedRows = bookClubMapper.updateBookClubSettings(
                bookClubSeq,
                newName,
                newDescription,
                trimToNull(dto.getRegion()),
                trimToNull(dto.getSchedule()),
                trimToNull(dto.getBannerImgUrl()));

        // 6. 업데이트 성공 확인
        if (updatedRows == 0) {
            log.warn("모임 설정 업데이트 실패: 업데이트 대상 없음 - bookClubSeq={}", bookClubSeq);
            throw new BookClubInvalidRequestException("설정 업데이트에 실패했습니다.");
        }

        // 7. 최신 book_club 조회 후 반환
        BookClubVO updatedBookClub = bookClubMapper.selectById(bookClubSeq);

        log.info("모임 설정 업데이트 완료: bookClubSeq={}, newName={}", bookClubSeq, newName);

        // 8. 배너가 교체되었으면 기존 S3 이미지 삭제 예약 (afterCommit)
        String newBannerUrl = updatedBookClub.getBanner_img_url();
        if (oldBannerUrl != null && !oldBannerUrl.equals(newBannerUrl)) {
            scheduleS3DeletionAfterCommit(oldBannerUrl);
        }

        // 프론트에서 사용할 업데이트된 정보 반환
        return java.util.Map.of(
                "name", updatedBookClub.getBook_club_name(),
                "description", updatedBookClub.getBook_club_desc(),
                "region", updatedBookClub.getBook_club_rg() != null ? updatedBookClub.getBook_club_rg() : "",
                "schedule",
                updatedBookClub.getBook_club_schedule() != null ? updatedBookClub.getBook_club_schedule() : "",
                "bannerImgUrl", newBannerUrl != null ? newBannerUrl : "");
    }

    /**
     * 문자열 trim 후 빈 문자열이면 null 반환
     * 정책A: 빈값은 null로 저장 (DB 정합성 + UI 일관성)
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * afterCommit에서 S3 이미지 삭제 스케줄링 (트랜잭션 커밋 후 실행)
     *
     * @param oldUrl 삭제할 기존 S3 URL
     */
    private void scheduleS3DeletionAfterCommit(String oldUrl) {
        // null/empty 체크
        if (oldUrl == null || oldUrl.trim().isEmpty()) {
            return;
        }

        // S3 URL인지 확인 (http/https로 시작하는 경우만)
        // 로컬 경로(/img/...)는 제외
        if (!oldUrl.startsWith("http://") && !oldUrl.startsWith("https://")) {
            log.debug("Skipping non-S3 URL deletion: {}", oldUrl);
            return;
        }

        // 트랜잭션이 활성화되어 있는지 확인 (Service 레이어이므로 항상 true)
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    deleteImageSafely(oldUrl);
                }
            });
            log.debug("Scheduled S3 deletion after commit: {}", oldUrl);
        } else {
            // Service 레이어에서는 거의 발생하지 않지만, 방어적으로 처리
            log.warn("No active transaction in Service layer, skipping S3 deletion: {}", oldUrl);
        }
    }

    /**
     * S3 이미지 삭제 실행 (실패해도 예외를 던지지 않음)
     * 커밋 후 실행되므로 실패해도 DB 트랜잭션에 영향 없음
     *
     * @param url 삭제할 S3 URL
     */
    private void deleteImageSafely(String url) {
        try {
            log.info("Deleting old image after commit: {}", url);
            imgService.deleteByUrl(url);
            log.info("Successfully deleted old image: {}", url);
        } catch (Exception e) {
            log.error("Failed to delete old image: {}, error: {}", url, e.getMessage(), e);
        }
    }

    // #5-7. 멤버 탈퇴 (모임장 탈퇴 시 승계 또는 모임 종료 포함)
    /**
     * 독서모임 멤버 탈퇴
     *
     * 검증 및 처리 순서:
     * 1. 파라미터 null 체크
     * 2. book_club 행 잠금 (동시성 제어)
     * 3. 멤버 상태 조회 (요구사항 SQL #1)
     * 4. join_st 상태 검증
     * 5-A. 일반 멤버 탈퇴: join_st='LEFT' 업데이트
     * 5-B. 모임장 탈퇴:
     * - JOINED 멤버가 있으면 자동 승계 (가장 오래된 멤버)
     * - JOINED 멤버가 없으면 모임 종료 (soft delete)
     *
     * @param bookClubSeq 독서모임 ID
     * @param memberSeq   탈퇴하려는 멤버 ID
     * @return 탈퇴 결과 Map (success, message, leaderChanged, newLeaderSeq, clubClosed,
     *         ctaStatus)
     * @throws IllegalArgumentException 파라미터가 null인 경우
     * @throws IllegalStateException    비즈니스 규칙 위반 시
     */
    @Transactional
    @CacheEvict(value = "bookClubList", allEntries = true)
    public java.util.Map<String, Object> leaveBookClub(Long bookClubSeq, Long memberSeq) {
        // 1. 파라미터 검증
        if (bookClubSeq == null || memberSeq == null) {
            log.warn("멤버 탈퇴 실패: 잘못된 파라미터 - bookClubSeq={}, memberSeq={}",
                    bookClubSeq, memberSeq);
            throw new BookClubInvalidRequestException("잘못된 요청입니다.");
        }

        // 2. book_club 행 잠금 (동시성 제어 - 모임장 탈퇴 승계/종료 경쟁 방지)
        Long lockedClubSeq = bookClubMapper.lockBookClubForUpdate(bookClubSeq);
        if (lockedClubSeq == null) {
            log.warn("멤버 탈퇴 실패: 모임이 존재하지 않거나 삭제됨 - bookClubSeq={}", bookClubSeq);
            throw new BookClubInvalidRequestException("존재하지 않거나 종료된 모임입니다.");
        }

        // 3. 멤버 상태 조회 (요구사항 SQL #1: leader_yn, join_st만 조회)
        java.util.Map<String, Object> memberStatus = bookClubMapper.selectMyMemberStatus(bookClubSeq, memberSeq);
        if (memberStatus == null) {
            log.warn("멤버 탈퇴 실패: 존재하지 않는 멤버 - bookClubSeq={}, memberSeq={}",
                    bookClubSeq, memberSeq);
            throw new BookClubInvalidRequestException("존재하지 않는 멤버입니다.");
        }

        // 4. join_st 상태 검증
        String joinSt = (String) memberStatus.get("join_st");
        if (!"JOINED".equals(joinSt)) {
            // LEFT 또는 KICKED
            if ("LEFT".equals(joinSt) || "KICKED".equals(joinSt)) {
                log.warn("멤버 탈퇴 실패: 이미 탈퇴/강퇴 - bookClubSeq={}, memberSeq={}, joinSt={}",
                        bookClubSeq, memberSeq, joinSt);
                throw new BookClubInvalidRequestException("이미 탈퇴했거나 강퇴된 멤버입니다.");
            }
            // WAIT, REJECTED 등 JOINED가 아닌 경우
            log.warn("멤버 탈퇴 실패: 탈퇴 불가 상태 - bookClubSeq={}, memberSeq={}, joinSt={}",
                    bookClubSeq, memberSeq, joinSt);
            throw new BookClubInvalidRequestException("탈퇴할 수 없는 상태입니다.");
        }

        // 5. 모임장 여부 판단 (Boolean.TRUE.equals로 안전하게 비교)
        Boolean leaderYn = (Boolean) memberStatus.get("leader_yn");
        boolean isLeader = Boolean.TRUE.equals(leaderYn);

        if (!isLeader) {
            // 4-A. 일반 멤버 탈퇴
            int updatedRows = bookClubMapper.updateMemberToLeft(bookClubSeq, memberSeq);
            if (updatedRows == 0) {
                log.warn("멤버 탈퇴 실패: 업데이트 대상 없음 - bookClubSeq={}, memberSeq={}",
                        bookClubSeq, memberSeq);
                throw new BookClubInvalidRequestException("탈퇴 처리에 실패했습니다.");
            }

            log.info("일반 멤버 탈퇴 완료: bookClubSeq={}, memberSeq={}", bookClubSeq, memberSeq);

            return java.util.Map.of(
                    "success", true,
                    "message", "탈퇴했습니다.",
                    "ctaStatus", "NONE");
        }

        // 4-B. 모임장 탈퇴
        // 승계 대상 조회 (leader 제외 JOINED 멤버 중 가장 오래된 1명)
        Long newLeaderSeq = bookClubMapper.selectNextLeaderCandidate(bookClubSeq);

        if (newLeaderSeq != null) {
            // 승계 대상이 있음 → 자동 승계
            // 1) 기존 모임장: leader_yn=false + join_st='LEFT'
            int leaderLeftRows = bookClubMapper.updateMemberToLeft(bookClubSeq, memberSeq);
            if (leaderLeftRows == 0) {
                log.warn("모임장 탈퇴 실패: 기존 모임장 LEFT 업데이트 실패 - bookClubSeq={}, memberSeq={}",
                        bookClubSeq, memberSeq);
                throw new BookClubInvalidRequestException("탈퇴 처리에 실패했습니다.");
            }

            // 2) 새 모임장: leader_yn=true
            int newLeaderRows = bookClubMapper.updateMemberToLeader(bookClubSeq, newLeaderSeq);
            if (newLeaderRows == 0) {
                log.warn("모임장 승계 실패: 새 모임장 설정 실패 - bookClubSeq={}, newLeaderSeq={}",
                        bookClubSeq, newLeaderSeq);
                throw new BookClubInvalidRequestException("모임장 승계에 실패했습니다.");
            }

            // 3) book_club.book_club_leader_seq 변경
            int clubUpdateRows = bookClubMapper.updateBookClubLeader(bookClubSeq, newLeaderSeq);
            if (clubUpdateRows == 0) {
                log.warn("모임장 승계 실패: book_club 리더 변경 실패 - bookClubSeq={}, newLeaderSeq={}",
                        bookClubSeq, newLeaderSeq);
                throw new BookClubInvalidRequestException("모임장 승계에 실패했습니다.");
            }

            log.info("모임장 탈퇴 + 승계 완료: bookClubSeq={}, oldLeaderSeq={}, newLeaderSeq={}",
                    bookClubSeq, memberSeq, newLeaderSeq);

            return java.util.Map.of(
                    "success", true,
                    "message", "탈퇴했고 모임장이 승계되었습니다.",
                    "leaderChanged", true,
                    "newLeaderSeq", newLeaderSeq,
                    "ctaStatus", "NONE");
        } else {
            // 승계 대상이 없음 → 모임 종료
            // 0) S3 삭제를 위해 배너 + 게시글 이미지 URL 미리 조회
            BookClubVO bookClub = bookClubMapper.selectById(bookClubSeq);
            String bannerUrl = (bookClub != null) ? bookClub.getBanner_img_url() : null;
            List<String> postImageUrls = bookClubMapper.selectAllBoardImageUrls(bookClubSeq);

            // 1) 모임장 LEFT 처리
            int leaderLeftRows = bookClubMapper.updateMemberToLeft(bookClubSeq, memberSeq);
            if (leaderLeftRows == 0) {
                log.warn("모임장 탈퇴 실패: 기존 모임장 LEFT 업데이트 실패 - bookClubSeq={}, memberSeq={}",
                        bookClubSeq, memberSeq);
                throw new BookClubInvalidRequestException("탈퇴 처리에 실패했습니다.");
            }

            // 2) 모임 종료 (soft delete)
            int closeRows = bookClubMapper.closeBookClub(bookClubSeq);
            if (closeRows == 0) {
                log.warn("모임 종료 실패: book_club 삭제 실패 - bookClubSeq={}", bookClubSeq);
                throw new BookClubInvalidRequestException("모임 종료에 실패했습니다.");
            }

            // 3) S3 이미지 일괄 삭제 예약 (afterCommit)
            scheduleS3DeletionAfterCommit(bannerUrl);
            if (postImageUrls != null && !postImageUrls.isEmpty()) {
                for (String imageUrl : postImageUrls) {
                    scheduleS3DeletionAfterCommit(imageUrl);
                }
            }

            log.info("모임장 탈퇴 + 모임 종료 완료: bookClubSeq={}, memberSeq={}", bookClubSeq, memberSeq);

            return java.util.Map.of(
                    "success", true,
                    "message", "모임이 종료되었습니다.",
                    "clubClosed", true);
        }
    }

    /*
     * #6. 게시글/댓글 좋아요
     */
    // #6-1. 좋아요 개수 조회
    public int getBoardLikeCount(Long boardSeq) {
        if (boardSeq == null) {
            return 0;
        }
        return bookClubMapper.selectBoardLikeCount(boardSeq);
    }

    // #6-2. 특정 멤버의 좋아요 여부 확인
    public boolean isBoardLiked(Long boardSeq, Long memberSeq) {
        if (boardSeq == null || memberSeq == null) {
            return false;
        }
        return bookClubMapper.selectBoardLikeExists(boardSeq, memberSeq) > 0;
    }

    // #6-3. 좋아요 토글 (좋아요 추가/취소)
    @Transactional
    public boolean toggleBoardLike(Long boardSeq, Long memberSeq) {
        if (boardSeq == null || memberSeq == null) {
            return false;
        }

        boolean currentlyLiked = isBoardLiked(boardSeq, memberSeq);

        if (currentlyLiked) {
            // 이미 좋아요한 상태 → 좋아요 취소
            bookClubMapper.deleteBoardLike(boardSeq, memberSeq);
            return false; // 좋아요 해제됨
        } else {
            // 좋아요 안한 상태 → 좋아요 추가
            bookClubMapper.insertBoardLike(boardSeq, memberSeq);
            return true; // 좋아요됨
        }
    }
}
