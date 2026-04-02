package project.bookclub.ENUM;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 독서모임 가입 신청 처리 결과 (애플리케이션 레벨 enum)
 *
 * DB 검증 기반 시나리오:
 * 1. ALREADY_JOINED: book_club_member.join_st = 'JOINED' (중복 가입 방지)
 * 2. ALREADY_REQUESTED: book_club_request.request_st = 'WAIT' (중복 신청 방지)
 * 3. SUCCESS: book_club_request INSERT 성공
 * 4. INVALID_PARAMETERS: 파라미터 null 체크 실패
 *
 * 참고:
 * - book_club_request 테이블에는 UNIQUE 제약이 없음 (bookclubDDL.md line 110-121)
 * - 따라서 DB 레벨에서 중복 신청을 막지 못함
 * - 반드시 비즈니스 로직(hasPendingRequest)으로 방어 필요
 */
@Getter
@RequiredArgsConstructor
public enum JoinRequestResult {
    /**
     * 가입 신청 성공
     */
    SUCCESS("가입 신청이 완료되었습니다."),

    /**
     * 이미 가입된 멤버 (book_club_member.join_st = 'JOINED')
     */
    ALREADY_JOINED("이미 가입된 모임입니다."),

    /**
     * 이미 신청한 상태 (book_club_request.request_st = 'WAIT')
     */
    ALREADY_REQUESTED("이미 신청하셨습니다. 승인 대기 중입니다."),

    /**
     * 잘못된 파라미터 (bookClubSeq 또는 memberSeq가 null)
     */
    INVALID_PARAMETERS("잘못된 요청입니다.");

    /**
     * 사용자에게 보여줄 메시지
     */
    private final String message;
}
