package project.bookclub.ENUM;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 독서모임 멤버 가입 상태 (DB의 join_st_enum과 매핑)
 *
 * DB DDL:
 * CREATE TYPE join_st_enum AS ENUM ('WAIT', 'JOINED', 'REJECTED', 'LEFT', 'KICKED');
 *
 * 테이블: book_club_member.join_st
 *
 * @see bookclubDDL.md line 95
 */
@Getter
@RequiredArgsConstructor
public enum JoinStatus {
    /**
     * 대기중 (즉시가입 방식에서 사용, 승인형에서는 미사용)
     */
    WAIT("WAIT"),

    /**
     * 가입완료 (정상 멤버)
     */
    JOINED("JOINED"),

    /**
     * 가입거절
     */
    REJECTED("REJECTED"),

    /**
     * 탈퇴 (본인이 나감)
     */
    LEFT("LEFT"),

    /**
     * 강퇴 (모임장이 내보냄)
     */
    KICKED("KICKED");

    /**
     * DB에 저장되는 값
     */
    private final String dbValue;
}
