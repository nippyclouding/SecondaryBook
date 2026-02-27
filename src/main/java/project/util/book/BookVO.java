package project.util.book;

import lombok.Data;

@Data
public class BookVO {
    private long book_seq;
    private String book_title;
    private String isbn;    // 책 식별 번호
    private String book_author; // 책 저자명
    private String book_publisher;  // 출판사명
    private String book_img;    // 썸네일 url
    private int book_org_price;  // 책 원가

    public BookVO(String isbn, String book_title, String book_author, String book_publisher, String book_img, int book_org_price) {
        this.isbn = isbn;
        this.book_title = book_title;
        this.book_author = book_author;
        this.book_publisher = book_publisher;
        this.book_img = book_img;
        this.book_org_price = book_org_price;
    }
}
