package project.trade;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;
import project.member.MemberVO;
import project.payment.PaymentVO;
import project.trade.ENUM.SafePaymentStatus;
import project.trade.ENUM.SaleStatus;

import java.util.List;

@Mapper
public interface TradeMapper {

    // List<TradeVO> findAll(); // 전체조회
    List<TradeVO> findAllWithPaging(@Param("size") int size, @Param("offset") int offset, @Param("searchVO") TradeVO searchVO);
    int countAll(@Param("searchVO") TradeVO searchVO);

    TradeVO findBySeq(@Param("trade_seq") long trade_seq);  // 상세조회
    TradeVO findBySeqForUpdate(@Param("trade_seq") long trade_seq); // 정산 신청용 (FOR UPDATE 락 획득)
    List<TradeImageVO> findImgUrl(@Param("trade_seq") long trade_seq);    // 이미지 url 조회
    int incrementViews(@Param("trade_seq") long trade_seq); // 조회수 증가
    int save(TradeVO tradeVO);  // 판매글 등록
    int update(TradeVO tradeVO); // 판매글 수정
    int delete(@Param("trade_seq") Long trade_seq);

    void updateStatus(@Param("trade_seq") Long trade_seq, @Param("sold") String sold, @Param("member_buyer_seq") Long member_buyer_seq);
    int countLike(@Param("trade_seq") long trade_seq, @Param("member_seq")long member_seq); // 좋아요 카운팅
    int saveLike(@Param("trade_seq") long trade_seq, @Param("member_seq") long member_seq); // 좋아요 추가
    int deleteLike(@Param("trade_seq") long trade_seq, @Param("member_seq") long member_seq); // 좋아요 삭제
    int countLikeAll(@Param("trade_seq") long trade_seq); // 좋아요 조회

    TradeVO findByChatRoomSeq(@Param("chat_room_seq") long chat_room_seq);

    List<TradeVO> findAllCategories(); // 카테고리 조회

    MemberVO findSellerInfo(@Param("trade_seq") long trade_seq);    // 판매자 정보조회
    List<TradeVO> findAllWishTrades(@Param("member_seq") long member_seq); // 프로필 찜 목록 조회

    List<TradeVO> findAllPurchaseTrades(@Param("member_seq") long member_seq); // 구매내역 조회

    List<TradeVO> findAllSaleTrades(@Param("member_seq") long member_seq,
                                    @Param("status") String status); // 판매내역 조회

    SafePaymentStatus findSafePaymentStatus(@Param("trade_seq") long trade_seq);
    int updateSafePaymentStatus(@Param("trade_seq") long trade_seq,
                                @Param("status") SafePaymentStatus status); // 안전 결제 상태 업데이트

    // 안전결제 만료 시간 관련
    int updateSafePaymentWithExpire(@Param("trade_seq") long trade_seq,
                                    @Param("status") SafePaymentStatus status, // 상태
                                    @Param("expire_minutes") int expire_minutes, // 만료시간
                                    @Param("pending_buyer_seq") long pending_buyer_seq); // 구매 진행 중인 구매자 seq

    Long findSafePaymentExpireSeconds(@Param("trade_seq") long trade_seq); // 만료까지 남은 초 조회

    Long findPendingBuyerSeq(@Param("trade_seq") long trade_seq);

    int resetExpiredSafePayments();


    // 판매자 수동 sold 변경 (계좌 거래용)
    int updateStatusToSold(@Param("trade_seq") long trade_seq,
                           @Param("member_seq") long member_seq);

    // 구매 확정 처리
    int confirmPurchase(@Param("trade_seq") long trade_seq,
                        @Param("member_seq") long member_seq);

    // 15일 지난 미확정 건 자동 확정 (스케줄러용)
    int autoConfirmExpiredPurchases();

    // 구매자의 안전결제 구매 내역 조회
    List<TradeVO> findPurchasesByBuyer(@Param("member_seq") long member_seq);

    int updateStatus(@Param("trade_seq") long trade_seq, @Param("saleStatus") SaleStatus saleStatus);

    int deleteAll(@Param("member_seq") long member_seq);

    int updatePurchaseCompleted(@Param("trade_seq") Long trade_seq, @Param("member_buyer_seq") long member_buyer_seq,
                                @Param("post_no") String post_no, @Param("addr_h") String addr_h, @Param("addr_d") String addr_d);

    // 이미지 조회
    List<TradeImageVO> findImgUrlByTradeSeqList(@Param("tradeSeqList") List<Long> tradeSeqList);

    PaymentVO findPaymentCheckInfo(@Param("trade_seq") Long trade_seq);

    List<String> findAllImageUrlsByMember(@Param("member_seq") long member_seq);

    // 회원 탈퇴 전 활성 결제 건 수 조회 (PENDING: 진행 중, COMPLETED: 구매 미확정)
    int countActivePaymentsByMember(@Param("member_seq") long member_seq);

    // 안전결제 리스트 조회 (관리자용)
    List<TradeVO> findAllSafePays();
}