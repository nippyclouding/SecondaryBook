package project.settlement;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import project.settlement.SettlementStatus;

@Mapper
public interface SettlementMapper {

    // 정산 신청 INSERT
    int insertSettlement(SettlementVO settlement);

    // trade_seq로 정산 내역 조회
    SettlementVO findByTradeSeq(@Param("trade_seq") long trade_seq);

    // settlement_seq로 정산 내역 조회 (재처리 시 trade_seq 확인용)
    SettlementVO findBySettlementSeq(@Param("settlement_seq") long settlement_seq);

    // 상태별 정산 목록 조회 (관리자용)
    List<SettlementVO> findByStatus(@Param("settlement_st") SettlementStatus settlement_st);

    // REQUESTED 상태인 건 전체 조회 (배치용)
    List<SettlementVO> findAllRequested();

    // 정산 완료 처리 (배치)
    int updateToCompleted(@Param("settlement_seq") long settlement_seq);

    // 잔액 부족으로 정산 실패 처리 (skip listener에서 호출)
    int updateToInsufficient(@Param("settlement_seq") long settlement_seq);

    // sb_trade_info의 settlement_st 업데이트
    int updateTradeSettlementSt(@Param("trade_seq") long trade_seq,
                                @Param("settlement_st") SettlementStatus settlement_st);

    // 관리자 잔액 조회 (FOR UPDATE - 배치 처리용, 락 필요 시만 사용)
    Long getAdminBalance(@Param("account_seq") long account_seq);

    // 관리자 잔액 조회 (읽기 전용 - 대시보드/감사로그용, 락 불필요)
    Long getAdminBalanceReadOnly(@Param("account_seq") long account_seq);

    // 관리자 잔액 차감 (배치 정산 처리 시)
    int updateAdminBalance(@Param("account_seq") long account_seq,
                           @Param("amount") int amount);

    // 관리자 잔액 증가 (구매자 결제 완료 시 - Toss 웹훅 수신 후 호출)
    int increaseAdminBalance(@Param("account_seq") long account_seq,
                             @Param("amount") int amount);

    // 관리자 계좌 로그 INSERT
    int insertAccountLog(@Param("account_seq") long account_seq,
                         @Param("settlement_seq") long settlement_seq,
                         @Param("amount") int amount,
                         @Param("balance_after") long balance_after,
                         @Param("description") String description);

    // COMPLETED 중 이체 미확인 목록 (관리자 화면에서 수동 이체 대상 목록)
    List<SettlementVO> findTransferPending();

    // 이체 미확인 총액 (관리자 화면 합계 표시용)
    long sumTransferPending();

    // 이체 완료 확인 (관리자가 수동 이체 후 클릭)
    int confirmTransfer(@Param("settlement_seq") long settlement_seq);

    // 정산 건수 조회 (관리자 대시보드)
    int countByStatus(@Param("settlement_st") SettlementStatus settlement_st);

    // 잔액 부족 건을 REQUESTED로 재설정 (관리자 수동 재처리)
    int resetToRequested(@Param("settlement_seq") long settlement_seq);
}
