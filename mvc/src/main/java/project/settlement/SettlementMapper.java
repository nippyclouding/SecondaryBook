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

    // sb_trade_info의 settlement_st 업데이트
    int updateTradeSettlementSt(@Param("trade_seq") long trade_seq,
                                @Param("settlement_st") SettlementStatus settlement_st);

    // 정산 완료 처리 (관리자가 수동 이체 후 클릭)
    int confirmTransfer(@Param("settlement_seq") long settlement_seq);

    // 정산 건수 조회 (관리자 대시보드)
    int countByStatus(@Param("settlement_st") SettlementStatus settlement_st);

}
