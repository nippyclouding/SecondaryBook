package project.trade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.chat.chatroom.ChatroomService;
import project.chat.message.MessageService;
import project.chat.pubsub.ChatMessagePublisher;
import project.util.imgUpload.ImgService;
import project.util.imgUpload.S3Service;
import project.util.exception.ForbiddenException;
import project.util.exception.trade.TradeNotFoundException;
import project.trade.ENUM.SafePaymentStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    TradeMapper tradeMapper;

    @Mock
    BookImgMapper bookImgMapper;

    @Mock
    S3Service s3Service;

    @Mock
    ImgService imgService;

    @Mock
    ChatroomService chatroomService;

    @Mock
    MessageService messageService;

    @Mock
    ChatMessagePublisher chatMessagePublisher;

    @InjectMocks
    TradeService tradeService;

    // ========== 테스트용 헬퍼 메서드 ==========

    private TradeVO createTrade(long sellerSeq, Long buyerSeq, SafePaymentStatus safePaymentSt) {
        TradeVO trade = new TradeVO();
        trade.setMember_seller_seq(sellerSeq);
        if (buyerSeq != null) {
            trade.setMember_buyer_seq(buyerSeq);
        }
        trade.setSafe_payment_st(safePaymentSt);
        return trade;
    }

    // ========================================================================
    // validateSellerOwnership - 판매자 본인 검증
    // ========================================================================
    @Nested
    @DisplayName("validateSellerOwnership - 판매자 본인 검증")
    class ValidateSellerOwnership {

        @Test
        @DisplayName("본인 거래 - 예외 없음")
        void 본인거래_예외없음() {
            // given
            when(tradeMapper.findBySeq(100L)).thenReturn(createTrade(1L, null, SafePaymentStatus.NONE));

            // when & then
            assertThatCode(() -> tradeService.validateSellerOwnership(100L, 1L))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("타인 거래 접근 - ForbiddenException")
        void 타인거래_ForbiddenException() {
            // given
            when(tradeMapper.findBySeq(100L)).thenReturn(createTrade(1L, null, SafePaymentStatus.NONE));

            // when & then
            assertThatThrownBy(() -> tradeService.validateSellerOwnership(100L, 999L))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("존재하지 않는 거래 - ForbiddenException")
        void 존재하지않는거래_ForbiddenException() {
            // given
            when(tradeMapper.findBySeq(100L)).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> tradeService.validateSellerOwnership(100L, 1L))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    // ========================================================================
    // validateBuyerOwnership - 구매자 본인 검증
    // ========================================================================
    @Nested
    @DisplayName("validateBuyerOwnership - 구매자 본인 검증")
    class ValidateBuyerOwnership {

        @Test
        @DisplayName("본인 구매건 - 예외 없음")
        void 본인구매건_예외없음() {
            // given
            when(tradeMapper.findBySeq(100L)).thenReturn(createTrade(1L, 2L, SafePaymentStatus.COMPLETED));

            // when & then
            assertThatCode(() -> tradeService.validateBuyerOwnership(100L, 2L))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("타인 구매건 접근 - ForbiddenException")
        void 타인구매건_ForbiddenException() {
            // given
            when(tradeMapper.findBySeq(100L)).thenReturn(createTrade(1L, 2L, SafePaymentStatus.COMPLETED));

            // when & then
            assertThatThrownBy(() -> tradeService.validateBuyerOwnership(100L, 999L))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("구매자가 없는 거래 - ForbiddenException")
        void 구매자없는거래_ForbiddenException() {
            // given - member_buyer_seq가 0 (long 기본값), Long으로 감싸면 0L != null이지만 buyerSeq != member_seq
            when(tradeMapper.findBySeq(100L)).thenReturn(createTrade(1L, null, SafePaymentStatus.NONE));

            // when & then
            assertThatThrownBy(() -> tradeService.validateBuyerOwnership(100L, 2L))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("존재하지 않는 거래 - ForbiddenException")
        void 존재하지않는거래_ForbiddenException() {
            // given
            when(tradeMapper.findBySeq(100L)).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> tradeService.validateBuyerOwnership(100L, 2L))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    // ========================================================================
    // validateCanModify - 수정 가능 여부 검증
    // ========================================================================
    @Nested
    @DisplayName("validateCanModify - 수정 가능 여부 검증")
    class ValidateCanModify {

        @Test
        @DisplayName("본인 거래 + NONE 상태 - 수정 가능")
        void NONE_수정가능() {
            // given
            when(tradeMapper.findBySeq(100L)).thenReturn(createTrade(1L, null, SafePaymentStatus.NONE));

            // when & then
            assertThatCode(() -> tradeService.validateCanModify(100L, 1L))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("안전결제 PENDING 상태 - 수정 불가")
        void PENDING_수정불가() {
            // given
            when(tradeMapper.findBySeq(100L)).thenReturn(createTrade(1L, null, SafePaymentStatus.PENDING));

            // when & then
            assertThatThrownBy(() -> tradeService.validateCanModify(100L, 1L))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("수정할 수 없습니다");
        }

        @Test
        @DisplayName("안전결제 COMPLETED 상태 - 수정 불가")
        void COMPLETED_수정불가() {
            // given
            when(tradeMapper.findBySeq(100L)).thenReturn(createTrade(1L, null, SafePaymentStatus.COMPLETED));

            // when & then
            assertThatThrownBy(() -> tradeService.validateCanModify(100L, 1L))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("수정할 수 없습니다");
        }

        @Test
        @DisplayName("타인 거래 - 수정 불가")
        void 타인거래_수정불가() {
            // given
            when(tradeMapper.findBySeq(100L)).thenReturn(createTrade(1L, null, SafePaymentStatus.NONE));

            // when & then
            assertThatThrownBy(() -> tradeService.validateCanModify(100L, 999L))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("권한이 없습니다");
        }

        @Test
        @DisplayName("존재하지 않는 거래 - TradeNotFoundException")
        void 존재하지않는거래_TradeNotFoundException() {
            // given
            when(tradeMapper.findBySeq(100L)).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> tradeService.validateCanModify(100L, 1L))
                    .isInstanceOf(TradeNotFoundException.class);
        }
    }

    // ========================================================================
    // validateCanDelete - 삭제 가능 여부 검증
    // ========================================================================
    @Nested
    @DisplayName("validateCanDelete - 삭제 가능 여부 검증")
    class ValidateCanDelete {

        @Test
        @DisplayName("본인 거래 + NONE 상태 - 삭제 가능")
        void NONE_삭제가능() {
            // given
            when(tradeMapper.findBySeq(100L)).thenReturn(createTrade(1L, null, SafePaymentStatus.NONE));

            // when & then
            assertThatCode(() -> tradeService.validateCanDelete(100L, 1L))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("안전결제 PENDING 상태 - 삭제 불가")
        void PENDING_삭제불가() {
            // given
            when(tradeMapper.findBySeq(100L)).thenReturn(createTrade(1L, null, SafePaymentStatus.PENDING));

            // when & then
            assertThatThrownBy(() -> tradeService.validateCanDelete(100L, 1L))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("삭제할 수 없습니다");
        }

        @Test
        @DisplayName("안전결제 COMPLETED 상태 - 삭제 불가")
        void COMPLETED_삭제불가() {
            // given
            when(tradeMapper.findBySeq(100L)).thenReturn(createTrade(1L, null, SafePaymentStatus.COMPLETED));

            // when & then
            assertThatThrownBy(() -> tradeService.validateCanDelete(100L, 1L))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("삭제할 수 없습니다");
        }

        @Test
        @DisplayName("타인 거래 - 삭제 불가")
        void 타인거래_삭제불가() {
            // given
            when(tradeMapper.findBySeq(100L)).thenReturn(createTrade(1L, null, SafePaymentStatus.NONE));

            // when & then
            assertThatThrownBy(() -> tradeService.validateCanDelete(100L, 999L))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("존재하지 않는 거래 - TradeNotFoundException")
        void 존재하지않는거래_TradeNotFoundException() {
            // given
            when(tradeMapper.findBySeq(100L)).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> tradeService.validateCanDelete(100L, 1L))
                    .isInstanceOf(TradeNotFoundException.class);
        }
    }

    // ========================================================================
    // search - 판매글 단일 조회
    // ========================================================================
    @Nested
    @DisplayName("search - 판매글 단일 조회")
    class Search {

        @Test
        @DisplayName("정상 조회 - 이미지 포함")
        void 정상조회_이미지포함() {
            // given
            TradeVO trade = createTrade(1L, null, SafePaymentStatus.NONE);
            trade.setTrade_seq(100L);

            TradeImageVO img1 = new TradeImageVO();
            img1.setImg_url("https://cdn.example.com/img1.jpg");
            TradeImageVO img2 = new TradeImageVO();
            img2.setImg_url("https://cdn.example.com/img2.jpg");

            when(tradeMapper.findBySeq(100L)).thenReturn(trade);
            when(tradeMapper.findImgUrl(100L)).thenReturn(Arrays.asList(img1, img2));

            // when
            TradeVO result = tradeService.search(100L);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTrade_img()).hasSize(2);
            verify(tradeMapper).findBySeq(100L);
            verify(tradeMapper).findImgUrl(100L);
        }

        @Test
        @DisplayName("정상 조회 - 이미지 없음")
        void 정상조회_이미지없음() {
            // given
            TradeVO trade = createTrade(1L, null, SafePaymentStatus.NONE);
            when(tradeMapper.findBySeq(100L)).thenReturn(trade);
            when(tradeMapper.findImgUrl(100L)).thenReturn(Collections.emptyList());

            // when
            TradeVO result = tradeService.search(100L);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTrade_img()).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 거래 - TradeNotFoundException")
        void 존재하지않는거래_TradeNotFoundException() {
            // given
            when(tradeMapper.findBySeq(100L)).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> tradeService.search(100L))
                    .isInstanceOf(TradeNotFoundException.class);
        }
    }

    // ========================================================================
    // upload - 판매글 등록
    // ========================================================================
    @Nested
    @DisplayName("upload - 판매글 등록")
    class Upload {

        @Test
        @DisplayName("이미지 포함 등록 - 성공")
        void 이미지포함_등록성공() {
            // given
            TradeVO trade = new TradeVO();
            trade.setTrade_seq(100L);
            trade.setImgUrls(Arrays.asList("https://cdn.example.com/img1.jpg", "https://cdn.example.com/img2.jpg"));

            when(tradeMapper.save(trade)).thenReturn(1);

            // when
            boolean result = tradeService.upload(trade);

            // then
            assertThat(result).isTrue();
            verify(tradeMapper).save(trade);
            verify(bookImgMapper, times(2)).save(anyString(), eq(100L));
        }

        @Test
        @DisplayName("이미지 없이 등록 - 성공")
        void 이미지없이_등록성공() {
            // given
            TradeVO trade = new TradeVO();
            trade.setImgUrls(null);
            when(tradeMapper.save(trade)).thenReturn(1);

            // when
            boolean result = tradeService.upload(trade);

            // then
            assertThat(result).isTrue();
            verify(tradeMapper).save(trade);
            verify(bookImgMapper, never()).save(anyString(), anyLong());
        }

        @Test
        @DisplayName("등록 실패 - false 반환")
        void 등록실패_false반환() {
            // given
            TradeVO trade = new TradeVO();
            when(tradeMapper.save(trade)).thenReturn(0);

            // when
            boolean result = tradeService.upload(trade);

            // then
            assertThat(result).isFalse();
        }
    }

    // ========================================================================
    // saveLike - 찜하기 토글
    // ========================================================================
    @Nested
    @DisplayName("saveLike - 찜하기 토글")
    class SaveLike {

        @Test
        @DisplayName("찜하기 안 한 상태 - 찜 추가, true 반환")
        void 찜추가_true() {
            // given
            TradeVO trade = new TradeVO();
            trade.setTrade_seq(100L);
            trade.setMember_seller_seq(99L); // 본인이 아닌 판매자
            when(tradeMapper.findBySeq(100L)).thenReturn(trade);
            when(tradeMapper.countLike(100L, 1L)).thenReturn(0);
            when(tradeMapper.saveLike(100L, 1L)).thenReturn(1);

            // when
            boolean result = tradeService.saveLike(100L, 1L);

            // then
            assertThat(result).isTrue();
            verify(tradeMapper).saveLike(100L, 1L);
            verify(tradeMapper, never()).deleteLike(anyLong(), anyLong());
        }

        @Test
        @DisplayName("이미 찜한 상태 - 찜 취소, false 반환")
        void 찜취소_false() {
            // given
            TradeVO trade = new TradeVO();
            trade.setTrade_seq(100L);
            trade.setMember_seller_seq(99L); // 본인이 아닌 판매자
            when(tradeMapper.findBySeq(100L)).thenReturn(trade);
            when(tradeMapper.countLike(100L, 1L)).thenReturn(1);

            // when
            boolean result = tradeService.saveLike(100L, 1L);

            // then
            assertThat(result).isFalse();
            verify(tradeMapper).deleteLike(100L, 1L);
            verify(tradeMapper, never()).saveLike(anyLong(), anyLong());
        }
    }

    // ========================================================================
    // 안전결제 관련
    // ========================================================================
    @Nested
    @DisplayName("안전결제 관련")
    class SafePayment {

        @Test
        @DisplayName("안전결제 요청 - PENDING 상태로 변경")
        void 안전결제요청_성공() {
            // given
            when(tradeMapper.updateSafePaymentWithExpire(100L, SafePaymentStatus.PENDING, 5, 2L)).thenReturn(1);

            // when
            boolean result = tradeService.requestSafePayment(100L, 2L);

            // then
            assertThat(result).isTrue();
            verify(tradeMapper).updateSafePaymentWithExpire(100L, SafePaymentStatus.PENDING, 5, 2L);
        }

        @Test
        @DisplayName("안전결제 취소 - NONE 상태로 복구")
        void 안전결제취소_NONE복구() {
            // when
            tradeService.cancelSafePayment(100L);

            // then
            verify(tradeMapper).updateSafePaymentStatus(100L, SafePaymentStatus.NONE);
        }

        @Test
        @DisplayName("만료 시간 조회 - 값이 있을 때")
        void 만료시간_값있음() {
            // given
            when(tradeMapper.findSafePaymentExpireSeconds(100L)).thenReturn(180L);

            // when
            long seconds = tradeService.getSafePaymentExpireSeconds(100L);

            // then
            assertThat(seconds).isEqualTo(180L);
        }

        @Test
        @DisplayName("만료 시간 조회 - null이면 0 반환")
        void 만료시간_null이면_0() {
            // given
            when(tradeMapper.findSafePaymentExpireSeconds(100L)).thenReturn(null);

            // when
            long seconds = tradeService.getSafePaymentExpireSeconds(100L);

            // then
            assertThat(seconds).isEqualTo(0L);
        }

        @Test
        @DisplayName("만료된 안전결제 리셋")
        void 만료된_안전결제_리셋() {
            // given
            when(tradeMapper.resetExpiredSafePayments()).thenReturn(3);

            // when
            int count = tradeService.resetExpiredSafePayments();

            // then
            assertThat(count).isEqualTo(3);
        }
    }

    // ========================================================================
    // confirmPurchase - 구매 확정
    // ========================================================================
    @Nested
    @DisplayName("confirmPurchase - 구매 확정")
    class ConfirmPurchase {

        @Test
        @DisplayName("정상 확정 - true 반환")
        void 정상확정_true() {
            // given
            when(tradeMapper.confirmPurchase(100L, 2L)).thenReturn(1);

            // when
            boolean result = tradeService.confirmPurchase(100L, 2L);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("이미 확정된 건 - false 반환")
        void 이미확정_false() {
            // given
            when(tradeMapper.confirmPurchase(100L, 2L)).thenReturn(0);

            // when
            boolean result = tradeService.confirmPurchase(100L, 2L);

            // then
            assertThat(result).isFalse();
        }
    }

    // ========================================================================
    // updateStatusToSold - 수동 판매완료
    // ========================================================================
    @Nested
    @DisplayName("updateStatusToSold - 수동 판매완료")
    class UpdateToSoldManually {

        @Test
        @DisplayName("정상 변경 - true 반환")
        void 정상변경_true() {
            // given
            when(tradeMapper.updateStatusToSold(100L, 1L)).thenReturn(1);

            // when
            boolean result = tradeService.updateStatusToSold(100L, 1L);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("변경 실패 - false 반환")
        void 변경실패_false() {
            // given
            when(tradeMapper.updateStatusToSold(100L, 1L)).thenReturn(0);

            // when
            boolean result = tradeService.updateStatusToSold(100L, 1L);

            // then
            assertThat(result).isFalse();
        }
    }

    // ========================================================================
    // searchAllWithPaging - 페이징 조회
    // ========================================================================
    @Nested
    @DisplayName("searchAllWithPaging - 페이징 조회")
    class SearchAllWithPaging {

        @Test
        @DisplayName("1페이지 조회 - offset 0으로 계산")
        void page1_offset_0() {
            // given
            TradeVO searchVO = new TradeVO();
            List<TradeVO> expected = Arrays.asList(new TradeVO(), new TradeVO());
            when(tradeMapper.findAllWithPaging(10, 0, searchVO)).thenReturn(expected);

            // when
            List<TradeVO> result = tradeService.searchAllWithPaging(1, 10, searchVO);

            // then
            assertThat(result).hasSize(2);
            verify(tradeMapper).findAllWithPaging(10, 0, searchVO);
        }

        @Test
        @DisplayName("3페이지 조회 - offset 20으로 계산")
        void page3_offset_20() {
            // given
            TradeVO searchVO = new TradeVO();
            when(tradeMapper.findAllWithPaging(10, 20, searchVO)).thenReturn(Collections.emptyList());

            // when
            List<TradeVO> result = tradeService.searchAllWithPaging(3, 10, searchVO);

            // then
            verify(tradeMapper).findAllWithPaging(10, 20, searchVO);
        }
    }

    // ========================================================================
    // autoConfirmExpiredPurchases - 15일 자동 확정
    // ========================================================================
    @Test
    @DisplayName("autoConfirmExpiredPurchases - 자동 확정 건수 반환")
    void 자동확정_건수반환() {
        // given
        when(tradeMapper.autoConfirmExpiredPurchases()).thenReturn(5);

        // when
        int count = tradeService.autoConfirmExpiredPurchases();

        // then
        assertThat(count).isEqualTo(5);
    }
}
