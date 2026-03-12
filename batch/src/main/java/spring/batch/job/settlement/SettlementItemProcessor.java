package spring.batch.job.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import spring.batch.settlement.SettlementVO;
import spring.batch.util.AesEncryptionUtil;

/**
 * 정산 ItemProcessor.
 * DB에서 읽어온 암호화된 계좌번호를 복호화하여 Writer가 평문을 사용할 수 있도록 한다.
 */
@RequiredArgsConstructor
public class SettlementItemProcessor implements ItemProcessor<SettlementVO, SettlementVO> {

    private final AesEncryptionUtil aesEncryptionUtil;

    @Override
    public SettlementVO process(SettlementVO item) {
        if (item.getBank_account_no() != null) {
            item.setBank_account_no(aesEncryptionUtil.decrypt(item.getBank_account_no()));
        }
        return item;
    }
}
