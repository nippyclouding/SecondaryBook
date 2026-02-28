package project.trade;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;
import project.trade.ENUM.BookStatus;
import project.trade.ENUM.PaymentType;
import project.trade.ENUM.SafePaymentStatus;
import project.trade.ENUM.SaleStatus;
import project.settlement.SettlementStatus;
import project.util.Const;
import project.util.book.BookVO;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.List;

@Data
public class TradeVO implements Serializable {
    // DB 컬럼명과 일치시킨 필드들
    private long trade_seq;
    private long member_seller_seq; // 추가
    private Long member_buyer_seq;  // 추가
    private long category_seq;      // 추가
    private long book_info_seq;     // 추가
    private long settlement_seq;    // 추가
    private Long pending_buyer_seq; // 추가

    @NotBlank
    private String sale_title;      //책 제목
    private BookStatus book_st;     // DB: book_st_enum 매핑
    @NotBlank @Length(max = 500)
    private String sale_cont;       // 상세설명
    @NotNull @Min(0) @Max(Integer.MAX_VALUE)
    private Integer sale_price;         // 판매가격
    @NotNull @Min(0) @Max(Integer.MAX_VALUE)
    private Integer delivery_cost; // 배송비
    private String sale_rg;         // 수정: book_sale_region -> sale_rg
    private SaleStatus sale_st;     // DB: sale_st_enum 매핑
    private LocalDateTime sale_st_dtm;  // 상품상태 변경 시간
    private long views;             // 조회수
    private long wish_cnt;          // 찜수
    private LocalDateTime book_sale_dtm;    //판매완료 시간
    private String post_no;         // 우편번호
    private String addr_h;          // 수정: address_h -> addr_h
    private String addr_d;          // 수정: address_d -> addr_d
    private String recipient_ph;    // 구매자 전화번호
    private String recipient_nm;    // 구매자 이름
//    private LocalDateTime crt_dtm;  // 완료일자
    private LocalDateTime upd_dtm;  // 업데이트 일자
    private PaymentType payment_type; // 거래방법
    @NotBlank
    private String category_nm; // 카테고리 이름

    private SettlementStatus settlement_st; // 정산 상태: NONE, READY, REQUESTED, COMPLETED
    // Book 관련 (Join 결과 매핑용)
    private String isbn;            // 책 고유번호
    @NotBlank
    private String book_title;      // 책 제목
    private String book_author;     // 저자
    private String book_publisher;  // 출판사
    @NotBlank
    private String book_img;        // 썸네일 이미지 url
    @NotNull @Min(0) @Max(Integer.MAX_VALUE)
    private Integer book_org_price;     // 책 원가

    // 이미지 리스트
    private transient List<MultipartFile> uploadFiles; // form 에서 받아오는 데이터 (직렬화 제외)
    private List<String> imgUrls; // db에 저장할 데이터
    private List<TradeImageVO> trade_img; // 화면 출력용
    private List<String> keepImageUrls; // 수정 시 유지할 기존 이미지 URL 목록

    // 검색용
    private String search_word;
    private String sort;

    private SafePaymentStatus safe_payment_st; // 안전결제 상태: NONE, PENDING, COMPLETED
    private LocalDateTime safe_payment_expire_dtm; // 안전결제 만료 시간

    private Boolean confirm_purchase;

    // 안전결제 출력용
    private String member_seller_nm;
    private String member_buyer_nm;

    public boolean checkTradeVO() {

        boolean result = false;

            if (sale_title != null && !sale_title.equals("") &&
                    book_img != null && !book_img.equals("") &&
                    book_title != null && !book_title.equals("") &&
                    // book_author != null && !book_author.equals("") &&
                    // book_publisher != null && !book_publisher.equals("") &&
                    category_nm != null && !category_nm.equals("") &&
                    sale_cont != null && !sale_cont.equals("") && !(sale_cont.length() >= 500)) {
                result = true;
            }
        return result;
    }

    public BookVO generateBook () {
        return new BookVO(isbn, book_title, book_author, book_publisher, book_img, book_org_price);
    }

    @JsonIgnore  // JSON 변환 시 제외
    private LocalDateTime crt_dtm;

    // JSON으로 반환할 포맷팅된 문자열
    @JsonProperty("crt_dtm")
    public String getCrtDtmFormatted() {
        return crt_dtm != null ? crt_dtm.format(Const.DATETIME_FORMATTER) : null;
    }
}