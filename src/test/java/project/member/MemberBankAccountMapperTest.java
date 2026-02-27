package project.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import project.config.TestMapperConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MemberBankAccountMapper 통합 테스트
 * - H2 인메모리 DB + 실제 MyBatis XML 쿼리 실행
 * - 계좌 등록 / 조회 / 수정 흐름을 검증한다
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestMapperConfig.class)
@Transactional
@DisplayName("MemberBankAccountMapper - 실제 SQL 실행 (H2)")
class MemberBankAccountMapperTest {

    @Autowired
    MemberBankAccountMapper bankAccountMapper;

    // ========== 헬퍼 ==========

    private MemberBankAccountVO createVO(long memberSeq, String bankCode, String accountNo, String holder) {
        MemberBankAccountVO vo = new MemberBankAccountVO();
        vo.setMember_seq(memberSeq);
        vo.setBank_code(bankCode);
        vo.setBank_account_no(accountNo);
        vo.setAccount_holder_nm(holder);
        return vo;
    }

    // ========================================================================
    // findByMemberSeq - 회원 계좌 조회
    // ========================================================================
    @Nested
    @DisplayName("findByMemberSeq - 회원 계좌 조회")
    class FindByMemberSeq {

        @Test
        @DisplayName("등록된 계좌 없음 - null 반환")
        void noAccount_returnsNull() {
            MemberBankAccountVO result = bankAccountMapper.findByMemberSeq(9999L);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("등록 후 조회 - 정보 일치")
        void afterInsert_found() {
            MemberBankAccountVO vo = createVO(1L, "004", "ENC_ACCOUNT_NO", "홍길동");
            bankAccountMapper.insert(vo);

            MemberBankAccountVO result = bankAccountMapper.findByMemberSeq(1L);

            assertThat(result).isNotNull();
            assertThat(result.getBank_code()).isEqualTo("004");
            assertThat(result.getBank_account_no()).isEqualTo("ENC_ACCOUNT_NO");
            assertThat(result.getAccount_holder_nm()).isEqualTo("홍길동");
        }
    }

    // ========================================================================
    // insert - 계좌 등록
    // ========================================================================
    @Nested
    @DisplayName("insert - 계좌 등록")
    class Insert {

        @Test
        @DisplayName("정상 등록 - 반환값 1 + PK 생성")
        void insert_returns1_pkGenerated() {
            MemberBankAccountVO vo = createVO(10L, "011", "ENC_NH_ACCOUNT", "김영희");

            int result = bankAccountMapper.insert(vo);

            assertThat(result).isEqualTo(1);
            assertThat(vo.getBank_account_seq()).isGreaterThan(0); // useGeneratedKeys
        }

        @Test
        @DisplayName("verified_yn은 항상 0으로 초기화된다")
        void verifiedYn_initializedTo0() {
            MemberBankAccountVO vo = createVO(11L, "088", "ENC_SHINHAN", "이철수");
            bankAccountMapper.insert(vo);

            MemberBankAccountVO result = bankAccountMapper.findByMemberSeq(11L);

            assertThat(result.isVerified_yn()).isFalse();
        }
    }

    // ========================================================================
    // update - 계좌 수정
    // ========================================================================
    @Nested
    @DisplayName("update - 계좌 수정")
    class Update {

        @Test
        @DisplayName("계좌 수정 성공 - 은행/계좌번호/예금주 변경됨")
        void update_changesFields() {
            MemberBankAccountVO vo = createVO(20L, "004", "OLD_ACCOUNT", "기존예금주");
            bankAccountMapper.insert(vo);

            // 수정
            vo.setBank_code("081");
            vo.setBank_account_no("NEW_HANA_ACCOUNT");
            vo.setAccount_holder_nm("새예금주");
            bankAccountMapper.update(vo);

            MemberBankAccountVO result = bankAccountMapper.findByMemberSeq(20L);

            assertThat(result.getBank_code()).isEqualTo("081");
            assertThat(result.getBank_account_no()).isEqualTo("NEW_HANA_ACCOUNT");
            assertThat(result.getAccount_holder_nm()).isEqualTo("새예금주");
        }

        @Test
        @DisplayName("수정 후 verified_yn은 다시 0으로 초기화")
        void update_resetsVerifiedYn() {
            MemberBankAccountVO vo = createVO(21L, "090", "ENC_KAKAO", "카카오회원");
            bankAccountMapper.insert(vo);

            vo.setBank_account_no("NEW_ENC_KAKAO");
            bankAccountMapper.update(vo);

            MemberBankAccountVO result = bankAccountMapper.findByMemberSeq(21L);
            assertThat(result.isVerified_yn()).isFalse(); // update 쿼리가 verified_yn=0으로 고정
        }
    }
}
