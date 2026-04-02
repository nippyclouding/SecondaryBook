package project.trade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import project.member.MemberVO;
import project.util.book.BookApiService;
import project.util.book.BookVO;
import project.util.imgUpload.FileStore;
import project.util.imgUpload.ImgService;
import project.util.imgUpload.S3Service;
import project.trade.ENUM.SafePaymentStatus;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TradeController 단위 테스트 (MockMvc standaloneSetup)
 * - 판매글 조회, 찜하기, 판매완료, 구매확정, 도서검색 등 핵심 엔드포인트 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TradeController")
class TradeControllerTest {

    @Mock TradeService tradeService;
    @Mock BookApiService bookApiService;
    @Mock FileStore fileStore;
    @Mock S3Service s3Service;
    @Mock ImgService imgService;

    @InjectMocks
    TradeController tradeController;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tradeController).build();
    }

    // ========== 헬퍼 ==========

    private MemberVO sessionMember(long seq) {
        MemberVO m = new MemberVO();
        m.setMember_seq(seq);
        m.setLogin_id("user" + seq);
        return m;
    }

    private MockHttpSession sessionWith(long memberSeq) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginSess", sessionMember(memberSeq));
        return session;
    }

    private TradeVO sampleTrade(long tradeSeq, long sellerSeq) {
        TradeVO trade = new TradeVO();
        trade.setTrade_seq(tradeSeq);
        trade.setMember_seller_seq(sellerSeq);
        trade.setSale_title("테스트 책");
        trade.setSale_price(10000);
        trade.setDelivery_cost(3000);
        trade.setSafe_payment_st(SafePaymentStatus.NONE);
        return trade;
    }

    // ========================================================================
    // GET /trade/{tradeSeq} - 판매글 상세 조회
    // ========================================================================
    @Nested
    @DisplayName("GET /trade/{tradeSeq} - 판매글 상세 조회")
    class TradeDetail {

        @Test
        @DisplayName("비로그인 - 찜 상태 false + 모델에 trade 포함")
        void notLoggedIn_showsDetail() throws Exception {
            TradeVO trade = sampleTrade(100L, 1L);
            when(tradeService.search(100L)).thenReturn(trade);
            when(tradeService.countLikeAll(100L)).thenReturn(5);
            when(tradeService.findSellerInfo(100L)).thenReturn(sessionMember(1L));

            mockMvc.perform(get("/trade/100"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("trade/tradedetail"))
                    .andExpect(model().attributeExists("trade"))
                    .andExpect(model().attribute("wishCount", 5))
                    .andExpect(model().attribute("wished", false));
        }

        @Test
        @DisplayName("로그인 - 본인 찜 여부 조회 후 모델에 포함")
        void loggedIn_checkWishedStatus() throws Exception {
            TradeVO trade = sampleTrade(100L, 1L);
            when(tradeService.search(100L)).thenReturn(trade);
            when(tradeService.countLikeAll(100L)).thenReturn(3);
            when(tradeService.findSellerInfo(100L)).thenReturn(sessionMember(1L));
            when(tradeService.isWished(100L, 2L)).thenReturn(true);

            mockMvc.perform(get("/trade/100").session(sessionWith(2L)))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("wished", true));
        }
    }

    // ========================================================================
    // POST /trade/like - 찜하기 토글
    // ========================================================================
    @Nested
    @DisplayName("POST /trade/like - 찜하기 토글")
    class TradeLike {

        @Test
        @DisplayName("찜 추가 - wished: true JSON 반환")
        void addLike_wishedTrue() throws Exception {
            when(tradeService.saveLike(100L, 1L)).thenReturn(true);

            mockMvc.perform(post("/trade/like")
                            .session(sessionWith(1L))
                            .param("trade_seq", "100")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"success\":true,\"wished\":true}"));
        }

        @Test
        @DisplayName("찜 취소 - wished: false JSON 반환")
        void removeLike_wishedFalse() throws Exception {
            when(tradeService.saveLike(100L, 1L)).thenReturn(false);

            mockMvc.perform(post("/trade/like")
                            .session(sessionWith(1L))
                            .param("trade_seq", "100")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"success\":true,\"wished\":false}"));
        }
    }

    // ========================================================================
    // POST /trade/sold - 판매자 수동 판매완료
    // ========================================================================
    @Nested
    @DisplayName("POST /trade/sold - 판매자 수동 판매완료")
    class TradeSold {

        @Test
        @DisplayName("판매완료 성공 - success: true")
        void success_returnsTrue() throws Exception {
            when(tradeService.updateStatusToSold(100L, 1L)).thenReturn(true);

            mockMvc.perform(post("/trade/sold")
                            .session(sessionWith(1L))
                            .param("trade_seq", "100")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"success\":true}"));
        }

        @Test
        @DisplayName("판매완료 실패 - success: false")
        void failure_returnsFalse() throws Exception {
            when(tradeService.updateStatusToSold(100L, 1L)).thenReturn(false);

            mockMvc.perform(post("/trade/sold")
                            .session(sessionWith(1L))
                            .param("trade_seq", "100")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"success\":false}"));
        }
    }

    // ========================================================================
    // POST /trade/confirm/{trade_seq} - 구매 확정
    // ========================================================================
    @Nested
    @DisplayName("POST /trade/confirm/{trade_seq} - 구매 확정")
    class ConfirmPurchase {

        @Test
        @DisplayName("구매 확정 성공 - success: true")
        void success_returnsTrue() throws Exception {
            when(tradeService.confirmPurchase(200L, 2L)).thenReturn(true);

            mockMvc.perform(post("/trade/confirm/200")
                            .session(sessionWith(2L)))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"success\":true}"));
        }

        @Test
        @DisplayName("이미 확정된 건 - success: false")
        void alreadyConfirmed_returnsFalse() throws Exception {
            when(tradeService.confirmPurchase(200L, 2L)).thenReturn(false);

            mockMvc.perform(post("/trade/confirm/200")
                            .session(sessionWith(2L)))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"success\":false}"));
        }
    }

    // ========================================================================
    // GET /trade/book - 도서 검색 (외부 API 위임)
    // ========================================================================
    @Nested
    @DisplayName("GET /trade/book - 도서 검색")
    class BookSearch {

        @Test
        @DisplayName("검색 결과 반환 - JSON 배열")
        void returnsBookList() throws Exception {
            BookVO b1 = new BookVO("isbn1", "클린 코드", "마틴", "인사이트",
                    "https://img/book1.jpg", 32000);
            BookVO b2 = new BookVO("isbn2", "리팩터링", "마틴", "한빛미디어",
                    "https://img/book2.jpg", 35000);
            List<BookVO> books = Arrays.asList(b1, b2);

            when(bookApiService.searchBooks("클린")).thenReturn(books);

            mockMvc.perform(get("/trade/book").param("query", "클린"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

            verify(bookApiService).searchBooks("클린");
        }

        @Test
        @DisplayName("검색 결과 없으면 빈 배열")
        void noResults_emptyArray() throws Exception {
            when(bookApiService.searchBooks("없는책")).thenReturn(List.of());

            mockMvc.perform(get("/trade/book").param("query", "없는책"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[]"));
        }
    }
}
