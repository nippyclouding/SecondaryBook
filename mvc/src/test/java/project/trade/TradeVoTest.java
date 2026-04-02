package project.trade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import project.util.book.BookVO;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TradeVO 도메인 로직 테스트
 * - checkTradeVO(): 필수 필드 유효성 확인 메서드
 * - generateBook(): TradeVO → BookVO 변환 메서드
 */
@DisplayName("TradeVO - 도메인 로직")
class TradeVoTest {

    // ========== 헬퍼 ==========

    private TradeVO validTradeVO() {
        TradeVO vo = new TradeVO();
        vo.setSale_title("자바 완전정복");
        vo.setBook_img("https://cdn.example.com/book.jpg");
        vo.setBook_title("자바 완전정복");
        vo.setCategory_nm("IT/컴퓨터");
        vo.setSale_cont("깨끗한 책입니다. 밑줄 없음.");
        return vo;
    }

    // ========================================================================
    // checkTradeVO - 필수 필드 검증
    // ========================================================================
    @Nested
    @DisplayName("checkTradeVO - 필수 필드 검증")
    class CheckTradeVO {

        @Test
        @DisplayName("모든 필수 필드가 있으면 true")
        void allFields_true() {
            assertThat(validTradeVO().checkTradeVO()).isTrue();
        }

        @Test
        @DisplayName("sale_title이 null이면 false")
        void saleTitle_null_false() {
            TradeVO vo = validTradeVO();
            vo.setSale_title(null);
            assertThat(vo.checkTradeVO()).isFalse();
        }

        @Test
        @DisplayName("sale_title이 빈 문자열이면 false")
        void saleTitle_blank_false() {
            TradeVO vo = validTradeVO();
            vo.setSale_title("");
            assertThat(vo.checkTradeVO()).isFalse();
        }

        @Test
        @DisplayName("book_img가 null이면 false")
        void bookImg_null_false() {
            TradeVO vo = validTradeVO();
            vo.setBook_img(null);
            assertThat(vo.checkTradeVO()).isFalse();
        }

        @Test
        @DisplayName("book_title이 null이면 false")
        void bookTitle_null_false() {
            TradeVO vo = validTradeVO();
            vo.setBook_title(null);
            assertThat(vo.checkTradeVO()).isFalse();
        }

        @Test
        @DisplayName("category_nm이 null이면 false")
        void categoryNm_null_false() {
            TradeVO vo = validTradeVO();
            vo.setCategory_nm(null);
            assertThat(vo.checkTradeVO()).isFalse();
        }

        @Test
        @DisplayName("sale_cont가 null이면 false")
        void saleCont_null_false() {
            TradeVO vo = validTradeVO();
            vo.setSale_cont(null);
            assertThat(vo.checkTradeVO()).isFalse();
        }

        @Test
        @DisplayName("sale_cont가 500자 이상이면 false")
        void saleCont_tooLong_false() {
            TradeVO vo = validTradeVO();
            vo.setSale_cont("a".repeat(500)); // length >= 500
            assertThat(vo.checkTradeVO()).isFalse();
        }

        @Test
        @DisplayName("sale_cont가 499자이면 true")
        void saleCont_maxValid_true() {
            TradeVO vo = validTradeVO();
            vo.setSale_cont("a".repeat(499)); // length < 500
            assertThat(vo.checkTradeVO()).isTrue();
        }
    }

    // ========================================================================
    // generateBook - BookVO 생성
    // ========================================================================
    @Nested
    @DisplayName("generateBook - BookVO 변환")
    class GenerateBook {

        @Test
        @DisplayName("TradeVO의 도서 정보가 BookVO에 정확히 매핑된다")
        void correctMapping() {
            TradeVO vo = new TradeVO();
            vo.setIsbn("9788966262557");
            vo.setBook_title("클린 코드");
            vo.setBook_author("로버트 마틴");
            vo.setBook_publisher("인사이트");
            vo.setBook_img("https://img.example.com/cover.jpg");
            vo.setBook_org_price(32000);

            BookVO book = vo.generateBook();

            assertThat(book.getIsbn()).isEqualTo("9788966262557");
            assertThat(book.getBook_title()).isEqualTo("클린 코드");
            assertThat(book.getBook_author()).isEqualTo("로버트 마틴");
            assertThat(book.getBook_publisher()).isEqualTo("인사이트");
            assertThat(book.getBook_img()).isEqualTo("https://img.example.com/cover.jpg");
            assertThat(book.getBook_org_price()).isEqualTo(32000);
        }

        @Test
        @DisplayName("String 필드가 null이어도 BookVO 생성 가능 (NPE 없음)")
        void nullFields_noException() {
            TradeVO vo = new TradeVO();
            vo.setBook_org_price(0); // int primitive - must not be null
            BookVO book = vo.generateBook();
            assertThat(book).isNotNull();
        }
    }
}
