package project.trade;

import lombok.Data;

@Data
public class TradeImageVO {
    private long book_img_seq;
    private long trade_seq;
    private String img_url;
    private int sort_seq;
}
