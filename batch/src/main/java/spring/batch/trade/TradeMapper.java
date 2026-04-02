package spring.batch.trade;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TradeMapper {

    int resetExpiredSafePayments();

    int autoConfirmExpiredPurchases();
}
