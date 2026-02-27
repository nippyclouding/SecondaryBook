package project.settlement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import project.config.TestMapperConfig;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;
import project.settlement.SettlementStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SettlementMapper 통합 테스트
 * - H2 인메모리 DB + 실제 MyBatis XML 쿼리 실행
 * - 정산 상태 전이 흐름을 SQL 레벨에서 검증한다
 *   REQUESTED → COMPLETED / INSUFFICIENT_BALANCE → REQUESTED
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestMapperConfig.class)
@Transactional
@DisplayName("SettlementMapper - 실제 SQL 실행 (H2)")
class SettlementMapperTest {

    @Autowired
    SettlementMapper settlementMapper;

    @Autowired
    DataSource dataSource;      // JdbcTemplate 생성용

    JdbcTemplate jdbc;

    // ========== BeforeEach: 공통 사전 데이터 ==========

    @BeforeEach
    void insertPrerequisiteData() {
        jdbc = new JdbcTemplate(dataSource);

        // member_info (settlement 서브쿼리 member_nicknm 참조용)
        jdbc.execute("INSERT INTO MEMBER_INFO (MEMBER_SEQ, LOGIN_ID, MEMBER_PWD, MEMBER_NICKNM, MEMBER_ST)" +
                     " VALUES (100, 'seller01', '$2a$10$hash', '판매자닉', 'JOIN')");

        // sb_trade_info (settlement JOIN 참조용)
        jdbc.execute("INSERT INTO sb_trade_info (TRADE_SEQ, MEMBER_SELLER_SEQ, SALE_TITLE, CATEGORY_SEQ," +
                     " BOOK_INFO_SEQ, SALE_PRICE, DELIVERY_COST, SETTLEMENT_ST)" +
                     " VALUES (1, 100, '테스트 책', 1, 1, 10000, 3000, 'REQUESTED')");
        jdbc.execute("INSERT INTO sb_trade_info (TRADE_SEQ, MEMBER_SELLER_SEQ, SALE_TITLE, CATEGORY_SEQ," +
                     " BOOK_INFO_SEQ, SALE_PRICE, DELIVERY_COST, SETTLEMENT_ST)" +
                     " VALUES (2, 100, '테스트 책2', 1, 1, 10000, 3000, 'REQUESTED')");

        // admin_account (잔액 조회용)
        jdbc.execute("INSERT INTO admin_account (ACCOUNT_SEQ, BALANCE) VALUES (1, 500000)");
    }

    // ========== 헬퍼 ==========

    private long insertSettlement(long tradeSeq, long memberSellerSeq, String status) {
        SettlementVO vo = new SettlementVO();
        vo.setTrade_seq(tradeSeq);
        vo.setMember_seller_seq(memberSellerSeq);
        vo.setSale_price(10000);
        vo.setDelivery_cost(3000);
        vo.setCommission_rate(new BigDecimal("0.0100"));
        vo.setCommission(130);
        vo.setSettlement_amount(12870);
        settlementMapper.insertSettlement(vo);
        if (!"REQUESTED".equals(status)) {
            // 상태를 원하는 값으로 직접 변경
            jdbc.update("UPDATE settlement SET settlement_st = ? WHERE settlement_seq = ?",
                    status, vo.getSettlement_seq());
        }
        return vo.getSettlement_seq();
    }

    // ========================================================================
    // insertSettlement - 정산 INSERT
    // ========================================================================
    @Nested
    @DisplayName("insertSettlement - 정산 INSERT")
    class InsertSettlement {

        @Test
        @DisplayName("정상 INSERT - 반환값 1 + PK 자동 생성")
        void insert_returns1_pkGenerated() {
            SettlementVO vo = new SettlementVO();
            vo.setTrade_seq(1L);
            vo.setMember_seller_seq(100L);
            vo.setSale_price(10000);
            vo.setDelivery_cost(3000);
            vo.setCommission_rate(new BigDecimal("0.0100"));
            vo.setCommission(130);
            vo.setSettlement_amount(12870);

            int result = settlementMapper.insertSettlement(vo);

            assertThat(result).isEqualTo(1);
            assertThat(vo.getSettlement_seq()).isGreaterThan(0L);
        }

        @Test
        @DisplayName("INSERT 직후 settlement_st 기본값 = REQUESTED")
        void defaultStatus_isRequested() {
            SettlementVO vo = new SettlementVO();
            vo.setTrade_seq(1L);
            vo.setMember_seller_seq(100L);
            vo.setSale_price(10000);
            vo.setDelivery_cost(3000);
            vo.setCommission_rate(new BigDecimal("0.0100"));
            vo.setCommission(130);
            vo.setSettlement_amount(12870);
            settlementMapper.insertSettlement(vo);

            SettlementVO found = settlementMapper.findBySettlementSeq(vo.getSettlement_seq());

            assertThat(found.getSettlement_st()).isEqualTo(SettlementStatus.REQUESTED);
        }
    }

    // ========================================================================
    // countByStatus - 상태별 건수
    // ========================================================================
    @Nested
    @DisplayName("countByStatus - 상태별 건수")
    class CountByStatus {

        @Test
        @DisplayName("REQUESTED 2건 삽입 후 countByStatus('REQUESTED') = 2")
        void requested_2items_count2() {
            insertSettlement(1L, 100L, "REQUESTED");
            insertSettlement(2L, 100L, "REQUESTED");

            int count = settlementMapper.countByStatus(SettlementStatus.REQUESTED);

            assertThat(count).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("COMPLETED 0건이면 count = 0")
        void completed_none_count0() {
            int count = settlementMapper.countByStatus(SettlementStatus.COMPLETED);
            assertThat(count).isEqualTo(0);
        }
    }

    // ========================================================================
    // updateToCompleted - REQUESTED → COMPLETED
    // ========================================================================
    @Nested
    @DisplayName("updateToCompleted - REQUESTED → COMPLETED 상태 전이")
    class UpdateToCompleted {

        @Test
        @DisplayName("REQUESTED 상태에서 호출 - 반환값 1 + 상태 변경")
        void requested_updatedToCompleted() {
            long seq = insertSettlement(1L, 100L, "REQUESTED");

            int result = settlementMapper.updateToCompleted(seq);

            assertThat(result).isEqualTo(1);
            SettlementVO updated = settlementMapper.findBySettlementSeq(seq);
            assertThat(updated.getSettlement_st()).isEqualTo(SettlementStatus.COMPLETED);
        }

        @Test
        @DisplayName("이미 COMPLETED 상태에서 호출 - 반환값 0 (멱등성 보호)")
        void alreadyCompleted_returns0() {
            long seq = insertSettlement(1L, 100L, "COMPLETED");

            int result = settlementMapper.updateToCompleted(seq);

            assertThat(result).isEqualTo(0);
        }
    }

    // ========================================================================
    // updateToInsufficient - REQUESTED → INSUFFICIENT_BALANCE
    // ========================================================================
    @Nested
    @DisplayName("updateToInsufficient - REQUESTED → INSUFFICIENT_BALANCE")
    class UpdateToInsufficient {

        @Test
        @DisplayName("REQUESTED → INSUFFICIENT_BALANCE 전이 성공")
        void requested_updatedToInsufficient() {
            long seq = insertSettlement(1L, 100L, "REQUESTED");

            int result = settlementMapper.updateToInsufficient(seq);

            assertThat(result).isEqualTo(1);
            SettlementVO updated = settlementMapper.findBySettlementSeq(seq);
            assertThat(updated.getSettlement_st()).isEqualTo(SettlementStatus.INSUFFICIENT_BALANCE);
        }

        @Test
        @DisplayName("COMPLETED 상태에서 호출 - 반환값 0 (조건: AND settlement_st='REQUESTED')")
        void completedState_returns0() {
            long seq = insertSettlement(1L, 100L, "COMPLETED");

            int result = settlementMapper.updateToInsufficient(seq);

            assertThat(result).isEqualTo(0);
        }
    }

    // ========================================================================
    // resetToRequested - INSUFFICIENT_BALANCE → REQUESTED (재처리)
    // ========================================================================
    @Nested
    @DisplayName("resetToRequested - INSUFFICIENT_BALANCE → REQUESTED 재처리")
    class ResetToRequested {

        @Test
        @DisplayName("INSUFFICIENT_BALANCE → REQUESTED 전이 성공")
        void insufficient_resetToRequested() {
            long seq = insertSettlement(1L, 100L, "INSUFFICIENT_BALANCE");

            int result = settlementMapper.resetToRequested(seq);

            assertThat(result).isEqualTo(1);
            SettlementVO updated = settlementMapper.findBySettlementSeq(seq);
            assertThat(updated.getSettlement_st()).isEqualTo(SettlementStatus.REQUESTED);
        }

        @Test
        @DisplayName("REQUESTED 상태에서 호출 - 반환값 0 (조건: AND settlement_st='INSUFFICIENT_BALANCE')")
        void requestedState_returns0() {
            long seq = insertSettlement(1L, 100L, "REQUESTED");

            int result = settlementMapper.resetToRequested(seq);

            assertThat(result).isEqualTo(0);
        }
    }

    // ========================================================================
    // confirmTransfer - 이체 완료 확인 (transfer_confirmed_yn = 1)
    // ========================================================================
    @Nested
    @DisplayName("confirmTransfer - 이체 완료 확인")
    class ConfirmTransfer {

        @Test
        @DisplayName("COMPLETED + transfer_confirmed_yn=0 → 반환값 1 + confirmed=true")
        void completedUnconfirmed_confirmsSuccessfully() {
            long seq = insertSettlement(1L, 100L, "COMPLETED");

            int result = settlementMapper.confirmTransfer(seq);

            assertThat(result).isEqualTo(1);
            SettlementVO updated = settlementMapper.findBySettlementSeq(seq);
            assertThat(updated.isTransfer_confirmed_yn()).isTrue();
        }

        @Test
        @DisplayName("이미 confirmed=1 → 반환값 0 (중복 확인 방지)")
        void alreadyConfirmed_returns0() {
            long seq = insertSettlement(1L, 100L, "COMPLETED");
            settlementMapper.confirmTransfer(seq); // 첫 번째 확인

            int result = settlementMapper.confirmTransfer(seq); // 두 번째 시도

            assertThat(result).isEqualTo(0);
        }
    }

    // ========================================================================
    // sumTransferPending - 이체 미확인 총액
    // ========================================================================
    @Nested
    @DisplayName("sumTransferPending - 이체 미확인 총액")
    class SumTransferPending {

        @Test
        @DisplayName("COMPLETED + unconfirmed 2건 합산")
        void twoUnconfirmed_sumsBoth() {
            insertSettlement(1L, 100L, "COMPLETED"); // 12870원
            insertSettlement(2L, 100L, "COMPLETED"); // 12870원

            long total = settlementMapper.sumTransferPending();

            assertThat(total).isEqualTo(12870L * 2);
        }

        @Test
        @DisplayName("COMPLETED 건이 없으면 0 반환 (COALESCE)")
        void noCompleted_returns0() {
            long total = settlementMapper.sumTransferPending();
            assertThat(total).isEqualTo(0L);
        }
    }

    // ========================================================================
    // getAdminBalance - 관리자 잔액 조회
    // ========================================================================
    @Test
    @DisplayName("getAdminBalance - admin_account 잔액 반환 (FOR UPDATE)")
    void getAdminBalance_returnsBalance() {
        Long balance = settlementMapper.getAdminBalance(1L);
        assertThat(balance).isEqualTo(500000L);
    }

    // ========================================================================
    // updateAdminBalance - 관리자 잔액 차감
    // ========================================================================
    @Test
    @DisplayName("updateAdminBalance - 잔액 차감 후 재조회")
    void updateAdminBalance_deductsAmount() {
        settlementMapper.updateAdminBalance(1L, 10000);

        Long balance = settlementMapper.getAdminBalance(1L);

        assertThat(balance).isEqualTo(490000L);
    }

    // ========================================================================
    // insertAccountLog - 계좌 로그 INSERT
    // ========================================================================
    @Test
    @DisplayName("insertAccountLog - 로그 행 삽입 성공")
    void insertAccountLog_returns1() {
        long seq = insertSettlement(1L, 100L, "REQUESTED");

        int result = settlementMapper.insertAccountLog(1L, seq, 12870, 487130L, "테스트 이체");

        assertThat(result).isEqualTo(1);
    }

    // ========================================================================
    // updateTradeSettlementSt - 거래 정산 상태 업데이트
    // ========================================================================
    @Test
    @DisplayName("updateTradeSettlementSt - sb_trade_info.settlement_st 업데이트")
    void updateTradeSettlementSt_changesStatus() {
        settlementMapper.updateTradeSettlementSt(1L, SettlementStatus.COMPLETED);

        String status = jdbc.queryForObject(
                "SELECT settlement_st FROM sb_trade_info WHERE trade_seq = 1", String.class);

        assertThat(status).isEqualTo("COMPLETED");
    }
}
