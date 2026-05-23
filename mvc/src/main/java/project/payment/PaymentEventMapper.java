package project.payment;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PaymentEventMapper {
    int save(PaymentEventVO event);
    List<PaymentEventVO> findUnresolvedConfirmUnknownEvents();
}
