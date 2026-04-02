package project.bookclub.ENUM;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 독서모임 가입 신청 상태 (DB의 request_st_enum과 매핑)
 *
 * DB DDL:
 * CREATE TYPE request_st_enum AS ENUM ('WAIT', 'APPROVED', 'REJECTED');
 *
 * 테이블: book_club_request.request_st
 *
 * @see bookclubDDL.md line 114
 */
@Getter
@RequiredArgsConstructor
public enum RequestStatus {
    /**
     * 승인 대기중 (신청 직후 기본값)
     */
    WAIT("WAIT"),

    /**
     * 승인됨 (모임장이 승인)
     */
    APPROVED("APPROVED"),

    /**
     * 거절됨 (모임장이 거절)
     */
    REJECTED("REJECTED");

    /**
     * DB에 저장되는 값
     */
    private final String dbValue;
}
