package project.trade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import project.member.MemberVO;
import project.payment.PaymentVO;
import project.trade.ENUM.SafePaymentStatus;
import project.trade.ENUM.SaleStatus;
import project.util.imgUpload.ImgService;
import project.chat.chatroom.ChatroomService;
import project.chat.message.MessageService;
import project.chat.message.MessageVO;
import project.chat.pubsub.ChatMessagePublisher;
import project.util.exception.ForbiddenException;
import project.util.exception.InvalidRequestException;
import project.util.exception.trade.TradeNotFoundException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class TradeService {
    private final TradeMapper tradeMapper;
    private final BookImgMapper bookImgMapper;
    private final ImgService imgService;
    private final ChatroomService chatroomService;
    private final MessageService messageService;
    private final ChatMessagePublisher chatMessagePublisher;

    @Value("${file.dir}")
    private String fileDir;

    public void validateSellerOwnership(long trade_seq, long member_seq) {
        TradeVO trade = tradeMapper.findBySeq(trade_seq);
        if (trade == null || trade.getMember_seller_seq() != member_seq) {
            if (trade == null) {
                log.warn("존재하지 않는 거래 접근: trade_seq={}, 요청자={}", trade_seq, member_seq);
            } else {
                log.warn("권한 없는 접근 시도: trade_seq={}, 요청자={}, 소유자={}",
                        trade_seq, member_seq, trade.getMember_seller_seq());
            }
            throw new ForbiddenException("권한이 없습니다.");
        }
    }

    public void validateBuyerOwnership(long trade_seq, long member_seq) {
        TradeVO trade = tradeMapper.findBySeq(trade_seq);
        Long buyerSeq = (trade != null) ? trade.getMember_buyer_seq() : null;
        if (trade == null || buyerSeq == null || buyerSeq != member_seq) {
            log.warn("권한 없는 접근 시도: trade_seq={}, 요청자={}", trade_seq, member_seq);
            throw new ForbiddenException("권한이 없습니다.");
        }
    }

    public void validateCanModify(long trade_seq, long member_seq) {
        TradeVO trade = tradeMapper.findBySeq(trade_seq);
        if (trade == null) {
            throw new TradeNotFoundException("거래를 찾을 수 없습니다. trade_seq=" + trade_seq);
        }
        if (trade.getMember_seller_seq() != member_seq) {
            throw new ForbiddenException("권한이 없습니다.");
        }
        SafePaymentStatus safePaymentStatus = trade.getSafe_payment_st();
        if (safePaymentStatus == SafePaymentStatus.PENDING || safePaymentStatus == SafePaymentStatus.COMPLETED) {
            throw new ForbiddenException("안전결제 진행 중이거나 완료된 거래는 수정할 수 없습니다.");
        }
    }

    public void validateCanDelete(long trade_seq, long member_seq) {
        TradeVO trade = tradeMapper.findBySeq(trade_seq);
        if (trade == null) {
            throw new TradeNotFoundException("거래를 찾을 수 없습니다. trade_seq=" + trade_seq);
        }
        if (trade.getMember_seller_seq() != member_seq) {
            throw new ForbiddenException("권한이 없습니다.");
        }
        SafePaymentStatus safePaymentStatus = trade.getSafe_payment_st();
        if (safePaymentStatus == SafePaymentStatus.PENDING || safePaymentStatus == SafePaymentStatus.COMPLETED) {
            throw new ForbiddenException("안전결제 진행 중이거나 완료된 거래는 삭제할 수 없습니다.");
        }
    }

    // 조회수 증가 (캐시 갱신 불필요 — 실시간 정확도보다 성능 우선)
    @Transactional
    public void incrementViews(long trade_seq) {
        tradeMapper.incrementViews(trade_seq);
    }

    // 판매글 단일 조회
    @Cacheable(value = "trade", key = "#trade_seq", unless = "#result == null")
    public TradeVO search(long trade_seq) {
        // 쿼리를 2번 조회하기 때문에 cache
        TradeVO findTrade = tradeMapper.findBySeq(trade_seq);

        if (findTrade == null) {
            log.info("데이터 조회 실패 : DB에서 데이터를 조회하지 못했습니다.");
            throw new TradeNotFoundException("Cannot find trade_seq=" + trade_seq);
        }

        List<TradeImageVO> imgUrl = tradeMapper.findImgUrl(trade_seq); // imgUrl은 null 이어도 된다 (썸네일 이미지만 보여진다)

        findTrade.setTrade_img(imgUrl);

        return findTrade;
    }

    // 리스트 : 페이징 조회
    @Cacheable(value = "tradeList",
            key = "'page:' + #page + ':size:' + #size + ':cat:' + #searchVO.category_seq + ':word:' +#searchVO.search_word + ':sort:' + #searchVO.sort + ':saleSt:' + #searchVO.sale_st + ':bookSt:' + #searchVO.book_st")
    public List<TradeVO> searchAllWithPaging(int page, int size, TradeVO searchVO) {
        int offset = (page - 1) * size;  // page가 1부터 시작한다고 가정
        return tradeMapper.findAllWithPaging(size, offset, searchVO);
    }

    // 전체 개수
    @Cacheable(value = "tradeCount",
            key = "'cat:' + #searchVO.category_seq + ':word:' + #searchVO.search_word + ':saleSt:' + #searchVO.sale_st + ':bookSt:' + #searchVO.book_st")
    public int countAll(TradeVO searchVO) {
        return tradeMapper.countAll(searchVO);
    }

    // 판매글 등록
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "tradeList", allEntries = true),
            @CacheEvict(value = "tradeCount", allEntries = true)
    })
    public boolean upload(TradeVO tradeVO) {

        int result = tradeMapper.save(tradeVO);

        // 등록된 이미지 처리
        if (tradeVO.getImgUrls() != null && !tradeVO.getImgUrls().isEmpty()) {
            for (String imgUrl : tradeVO.getImgUrls()) {
                bookImgMapper.save(imgUrl, tradeVO.getTrade_seq());
            }
        }
        return result > 0;
    }

    // 판매글 수정
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "trade", key = "#trade_seq"),
            @CacheEvict(value = "tradeList", allEntries = true),
            @CacheEvict(value = "tradeCount", allEntries = true)
    })
    public boolean modify(Long trade_seq, TradeVO updateTrade) {
        // tradeSeq : 기존 trade의 seq, updateTrade : 변경값을 담은 trade 객체
        // 변경하려는 trade에 현재 seq을 넣기
        updateTrade.setTrade_seq(trade_seq);

        int result = tradeMapper.update(updateTrade);

        // 이미지 처리
        List<String> newImgUrls = updateTrade.getImgUrls();
        if (newImgUrls == null) newImgUrls = new ArrayList<>();

        // 기존 이미지 목록 조회
        List<TradeImageVO> existingImages = tradeMapper.findImgUrl(trade_seq);

        // S3에서 삭제할 이미지 찾기 (기존 이미지 중 새 목록에 없는 것) - 트랜잭션 커밋 후 삭제
        if (existingImages != null && !existingImages.isEmpty()) {
            for (TradeImageVO existingImg : existingImages) {
                String existingUrl = existingImg.getImg_url();
                if (!newImgUrls.contains(existingUrl)) {
                    scheduleImageDeletionAfterCommit(existingUrl);
                }
            }
        }

        // DB에서 기존 이미지 전부 삭제
        bookImgMapper.deleteBySeq(trade_seq);

        // 새 이미지 목록이 있으면 저장 (유지할 이미지 + 새로 업로드한 이미지)
        if (!newImgUrls.isEmpty()) {
            for (String imgUrl : newImgUrls) {
                bookImgMapper.save(imgUrl, trade_seq);
            }
        }
        return result > 0;
    }


    // 판매글 삭제
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "trade", key = "#trade_seq"),
            @CacheEvict(value = "tradeList", allEntries = true),
            @CacheEvict(value = "tradeCount", allEntries = true)
    })
    public boolean remove(Long trade_seq) {

        // 이미지 url 조회 - 트랜잭션 커밋 후 삭제
        List<TradeImageVO> imgUrls = tradeMapper.findImgUrl(trade_seq);
        if (imgUrls != null && !imgUrls.isEmpty()) {
            for (TradeImageVO vo : imgUrls) {
                String imgUrl = vo.getImg_url();
                if (imgUrl != null) {
                    scheduleImageDeletionAfterCommit(imgUrl);
                }
            }
        }

        bookImgMapper.deleteBySeq(trade_seq); // 기존 이미지 url들을 db에서 먼저 삭제

        int result = tradeMapper.delete(trade_seq);

        return result > 0;
    }

    // 카테고리 조회
    public List<TradeVO> findAllCategories() {
        return tradeMapper.findAllCategories();
    }

    // 찜하기 insert
    @Transactional
    public boolean saveLike(long trade_seq, long member_seq) {
        TradeVO trade = tradeMapper.findBySeq(trade_seq);
        if (trade == null || Long.valueOf(member_seq).equals(trade.getMember_seller_seq())) {
            return false; // 본인 판매글 찜 방지
        }
        int cnt = tradeMapper.countLike(trade_seq, member_seq);
        if (cnt > 0) {  // 이미 누른 카운트가 있다면 delete하고 false 리턴
            tradeMapper.deleteLike(trade_seq, member_seq);
            return false;
        } else {        // 좋아요 누른게 없다면 true
            tradeMapper.saveLike(trade_seq, member_seq);
            return true;
        }
    }

    // 찜하기 이전에 눌렀는지 조회
    public boolean isWished(long trade_seq, long member_seq) {
        return tradeMapper.countLike(trade_seq, member_seq) > 0;
    }
    // 찜하기 전체 갯수 조회
    public int countLikeAll(long trade_seq) {
        return tradeMapper.countLikeAll(trade_seq);
    }

    // chat_room_seq 으로 trade 찾기
    public TradeVO findByChatRoomSeq(long chat_room_seq) {
        return tradeMapper.findByChatRoomSeq(chat_room_seq);
    }

    // 판매자 정보조회
    public MemberVO findSellerInfo(long tradeSeq) {
        return tradeMapper.findSellerInfo(tradeSeq);
    }
    public List<TradeVO> getWishTrades(long member_seq) {
        return tradeMapper.findAllWishTrades(member_seq);
    }

    // 구매내역
    public List<TradeVO> getPurchaseTrades(long member_seq) {
        return tradeMapper.findAllPurchaseTrades(member_seq);
    }

    // 판매내역
    public List<TradeVO> getSaleTrades(long member_seq, String status) {
        return tradeMapper.findAllSaleTrades(member_seq, status);
    }

    // 안전 결제 상태 조회 (단순 SELECT, readOnly 트랜잭션으로 충분)
    @Transactional(readOnly = true)
    public SafePaymentStatus getSafePaymentStatus(long trade_seq) {
        return tradeMapper.findSafePaymentStatus(trade_seq);
    }

    // 회원 탈퇴 전 활성 결제 건 존재 여부 확인
    // PENDING: 구매자가 결제 진행 중 / COMPLETED: 결제 완료 후 구매 미확정 상태
    public boolean hasActivePaymentsByMember(long member_seq) {
        return tradeMapper.countActivePaymentsByMember(member_seq) > 0;
    }

    // 안전 결제 요청 처리 (트랜잭션으로 상태 체크, 업데이트 원자적 처리), 5분 만료 시간 설정
    // return true : 안전 결제 요청 성공, false : 이미 안전 결제 요청 처리
    @Transactional
    @CacheEvict(value = "trade", key = "#trade_seq")
    public boolean requestSafePayment(long trade_seq, long pending_buyer_seq) {
        // 안전 결제 진행 중이 아닌 상태라면 PENDING(안전 결제 시작) 으로 변경 + 5분 만료 시간 설정
        int updated = tradeMapper.updateSafePaymentWithExpire(trade_seq, SafePaymentStatus.PENDING, 5, pending_buyer_seq);
        return updated > 0; // 0이면 이미 다른 사용자가 PENDING 중
    }

    // 안전 결제 실패, NONE 으로 update,  채팅방으로 다시 돌아가도록 하기
    @Transactional
    @CacheEvict(value = "trade", key = "#trade_seq")
    public void cancelSafePayment(long trade_seq) {
        tradeMapper.updateSafePaymentStatus(trade_seq, SafePaymentStatus.NONE);
    }


    public long getSafePaymentExpireSeconds(long trade_seq) {
        Long seconds = tradeMapper.findSafePaymentExpireSeconds(trade_seq); // trade의 안전 결제 만료 시간이 몇 초 남았는지 조회

        if (seconds == null) return 0;
        else return seconds;
    }

    @Transactional
    @CacheEvict(value = "trade", allEntries = true)
    public int resetExpiredSafePayments() {
        return tradeMapper.resetExpiredSafePayments();
    }


    // 판매자 수동 sold 변경
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "trade", key = "#trade_seq"),
            @CacheEvict(value = "tradeList", allEntries = true),
            @CacheEvict(value = "tradeCount", allEntries = true)
    })
    public boolean updateStatusToSold(long trade_seq, long member_seq) {
        return tradeMapper.updateStatusToSold(trade_seq, member_seq) > 0;
    }

    // 구매 확정
    @Transactional
    @CacheEvict(value = "trade", key = "#trade_seq")
    public boolean confirmPurchase(long trade_seq, long member_seq) {
        return tradeMapper.confirmPurchase(trade_seq, member_seq) > 0;
    }

    // 15일 지난 미확정 건 자동 확정
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "trade", allEntries = true),
            @CacheEvict(value = "tradeList", allEntries = true),
            @CacheEvict(value = "tradeCount", allEntries = true)
    })
    public int autoConfirmExpiredPurchases() {
        return tradeMapper.autoConfirmExpiredPurchases();
    }

    // 판매 상태 수동 업데이트 (SOLD)
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "trade", key = "#trade_seq"),
            @CacheEvict(value = "tradeList", allEntries = true),
            @CacheEvict(value = "tradeCount", allEntries = true)
    })
    public boolean updateStatus(long trade_seq) {
        return tradeMapper.updateStatus(trade_seq, SaleStatus.SOLD) > 0;
    }

    // 임의 SaleStatus로 상태 변경 (SALE / RESERVED / SOLD)
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "trade", key = "#trade_seq"),
            @CacheEvict(value = "tradeList", allEntries = true),
            @CacheEvict(value = "tradeCount", allEntries = true)
    })
    public boolean updateSaleStatus(long trade_seq, SaleStatus newStatus) {
        return tradeMapper.updateStatus(trade_seq, newStatus) > 0;
    }

    /**
     * 구매 확정 + 결제 완료 메시지 전송을 하나의 트랜잭션으로 통합
     */
    @Transactional
    @CacheEvict(value = "trade", key = "#tradeSeq")
    public void completePurchaseAndNotify(Long tradeSeq, long buyerSeq, String postNo, String addrH, String addrD) {
        // 1. 구매 확정 처리 (상태 가드로 중복 처리 방지)
        int updated = tradeMapper.updatePurchaseCompleted(tradeSeq, buyerSeq, postNo, addrH, addrD);
        if (updated == 0) {
            throw new InvalidRequestException("결제 처리 실패: 이미 처리되었거나 유효하지 않은 거래입니다. trade_seq=" + tradeSeq);
        }

        // 2. 채팅방 조회 후 결제 완료 메시지 저장
        Long chatRoomSeq = chatroomService.findChatRoomSeqByTradeAndBuyer(tradeSeq, buyerSeq);
        if (chatRoomSeq != null) {
            MessageVO completeMsg = new MessageVO();
            completeMsg.setChat_room_seq(chatRoomSeq);
            completeMsg.setSender_seq(buyerSeq);
            completeMsg.setChat_cont("[SAFE_PAYMENT_COMPLETE]");
            messageService.saveMessage(completeMsg);

            // Pub/Sub은 트랜잭션 커밋 후 전송 (실패해도 트랜잭션에 영향 없음)
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        chatMessagePublisher.publishPayment(chatRoomSeq, completeMsg);
                    } catch (Exception e) {
                        log.error("결제 완료 메시지 Pub/Sub 전송 실패: trade_seq={}, chatRoomSeq={}", tradeSeq, chatRoomSeq, e);
                    }
                }
            });
        } else {
            log.warn("채팅방을 찾을 수 없음: trade_seq={}", tradeSeq);
        }
    }
    // 결제창 진입 시 유효성 검사 항목
    @Transactional
    public PaymentVO getPaymentCheckInfo(Long trade_seq) {
        return tradeMapper.findPaymentCheckInfo(trade_seq);
    }


    @Caching(evict = {
            @CacheEvict(value = "trade", allEntries = true),
            @CacheEvict(value = "tradeList", allEntries = true),
            @CacheEvict(value = "tradeCount", allEntries = true)
    })
    @Transactional
    public int deleteAllByMember(long member_seq) { // 회원이 탈퇴 시 회원이 작성한 trade 삭제
        // 1. 해당 회원의 모든 Trade 이미지 URL 한번에 조회
        List<String> allImageUrls =
                tradeMapper.findAllImageUrlsByMember(member_seq);

        // 2. 이미지 삭제 (트랜잭션 커밋 후)
        if (allImageUrls != null && !allImageUrls.isEmpty()) {
            for (String imgUrl : allImageUrls) {
                if (imgUrl != null) {
                    scheduleImageDeletionAfterCommit(imgUrl);
                }
            }
        }

        // 3. 거래 글 소프트 삭제
        return tradeMapper.deleteAll(member_seq);
    }

    public List<TradeVO> findAllSafePays() {
        return tradeMapper.findAllSafePays();
    }

    private void scheduleImageDeletionAfterCommit(String url) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    int maxRetries = 3;
                    for (int attempt = 1; attempt <= maxRetries; attempt++) {
                        try {
                            imgService.deleteByUrl(url);
                            log.info("Deleted image after commit: {}", url);
                            return;
                        } catch (Exception e) {
                            if (attempt == maxRetries) {
                                log.error("Failed to delete image after {} attempts (orphaned): {}, error: {}", maxRetries, url, e.getMessage());
                            } else {
                                log.warn("Image delete attempt {}/{} failed, retrying: {}", attempt, maxRetries, url);
                            }
                        }
                    }
                }
            });
        } else {
            log.warn("No active transaction, skipping image deletion: {}", url);
        }
    }
}