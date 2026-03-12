package spring.batch.settlement;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SettlementMapper {

    List<SettlementVO> findAllRequested();

    SettlementVO findBySettlementSeq(@Param("settlement_seq") long settlement_seq);

    int updateToCompleted(@Param("settlement_seq") long settlement_seq);

    int updateToInsufficient(@Param("settlement_seq") long settlement_seq);

    int updateTradeSettlementSt(@Param("trade_seq") long trade_seq,
                                @Param("settlement_st") SettlementStatus settlement_st);

    Long getAdminBalance(@Param("account_seq") long account_seq);

    int updateAdminBalance(@Param("account_seq") long account_seq,
                           @Param("amount") int amount);

    int insertAccountLog(@Param("account_seq") long account_seq,
                         @Param("settlement_seq") long settlement_seq,
                         @Param("amount") int amount,
                         @Param("balance_after") long balance_after,
                         @Param("description") String description);
}
