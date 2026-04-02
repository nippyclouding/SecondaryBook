package spring.batch.job.settlement;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import spring.batch.settlement.SettlementVO;
import spring.batch.util.AesEncryptionUtil;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementItemProcessorTest {

    @InjectMocks
    private SettlementItemProcessor processor;

    @Mock
    private AesEncryptionUtil aesEncryptionUtil;

    @Test
    @DisplayName("계좌번호가 있으면 복호화 후 반환")
    void decryptsAccountNo() throws Exception {
        SettlementVO item = new SettlementVO();
        item.setSettlement_seq(1L);
        item.setBank_account_no("ENCRYPTED_ACCOUNT");

        when(aesEncryptionUtil.decrypt("ENCRYPTED_ACCOUNT")).thenReturn("123456789012");

        SettlementVO result = processor.process(item);

        assertThat(result.getBank_account_no()).isEqualTo("123456789012");
        verify(aesEncryptionUtil).decrypt("ENCRYPTED_ACCOUNT");
    }

    @Test
    @DisplayName("계좌번호가 null이면 복호화 미호출, 그대로 반환")
    void skipsDecryptWhenAccountNoIsNull() throws Exception {
        SettlementVO item = new SettlementVO();
        item.setSettlement_seq(1L);
        item.setBank_account_no(null);

        SettlementVO result = processor.process(item);

        assertThat(result.getBank_account_no()).isNull();
        verify(aesEncryptionUtil, never()).decrypt(anyString());
    }

    @Test
    @DisplayName("같은 SettlementVO 객체를 반환 (pass-through)")
    void returnsSameInstance() throws Exception {
        SettlementVO item = new SettlementVO();
        item.setSettlement_seq(1L);
        item.setBank_account_no("ENC");

        when(aesEncryptionUtil.decrypt("ENC")).thenReturn("111111");

        SettlementVO result = processor.process(item);

        assertThat(result).isSameAs(item);
    }
}
