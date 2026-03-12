package project.member;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import project.util.AesEncryptionUtil;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberBankAccountServiceTest {

    @InjectMocks
    private MemberBankAccountService service;

    @Mock
    private MemberBankAccountMapper mapper;

    @Mock
    private AesEncryptionUtil aesEncryptionUtil;

    // ========== save ==========

    @Nested
    @DisplayName("save - 계좌 등록/수정")
    class Save {

        @Test
        @DisplayName("신규 등록 - 계좌번호 암호화 후 insert 호출")
        void insertWhenNoExisting() {
            MemberBankAccountVO vo = new MemberBankAccountVO();
            vo.setMember_seq(1L);
            vo.setBank_code("004");
            vo.setBank_account_no("123456789012");
            vo.setAccount_holder_nm("홍길동");

            when(mapper.findByMemberSeq(1L)).thenReturn(null);
            when(aesEncryptionUtil.encrypt("123456789012")).thenReturn("ENCRYPTED");

            service.save(vo);

            // 암호화된 값으로 저장
            assertThat(vo.getBank_account_no()).isEqualTo("ENCRYPTED");
            verify(mapper).insert(vo);
            verify(mapper, never()).update(any());
        }

        @Test
        @DisplayName("기존 계좌 수정 - 계좌번호 암호화 후 update 호출")
        void updateWhenExisting() {
            MemberBankAccountVO existing = new MemberBankAccountVO();
            existing.setBank_account_seq(99L);

            MemberBankAccountVO vo = new MemberBankAccountVO();
            vo.setMember_seq(1L);
            vo.setBank_code("020");
            vo.setBank_account_no("999888777666");
            vo.setAccount_holder_nm("김철수");

            when(mapper.findByMemberSeq(1L)).thenReturn(existing);
            when(aesEncryptionUtil.encrypt("999888777666")).thenReturn("ENCRYPTED_NEW");

            service.save(vo);

            assertThat(vo.getBank_account_no()).isEqualTo("ENCRYPTED_NEW");
            assertThat(vo.getBank_account_seq()).isEqualTo(99L); // 기존 seq 유지
            verify(mapper).update(vo);
            verify(mapper, never()).insert(any());
        }
    }

    // ========== getByMemberSeq ==========

    @Nested
    @DisplayName("getByMemberSeq - 계좌 조회")
    class GetByMemberSeq {

        @Test
        @DisplayName("계좌 존재 시 계좌번호 복호화 후 반환")
        void decryptOnRead() {
            MemberBankAccountVO stored = new MemberBankAccountVO();
            stored.setMember_seq(1L);
            stored.setBank_code("004");
            stored.setBank_account_no("ENCRYPTED");
            stored.setAccount_holder_nm("홍길동");

            when(mapper.findByMemberSeq(1L)).thenReturn(stored);
            when(aesEncryptionUtil.decrypt("ENCRYPTED")).thenReturn("123456789012");

            MemberBankAccountVO result = service.getByMemberSeq(1L);

            assertThat(result.getBank_account_no()).isEqualTo("123456789012");
            verify(aesEncryptionUtil).decrypt("ENCRYPTED");
        }

        @Test
        @DisplayName("계좌 미등록 시 null 반환, 복호화 미호출")
        void returnsNullWhenNotFound() {
            when(mapper.findByMemberSeq(1L)).thenReturn(null);

            MemberBankAccountVO result = service.getByMemberSeq(1L);

            assertThat(result).isNull();
            verify(aesEncryptionUtil, never()).decrypt(anyString());
        }
    }
}
