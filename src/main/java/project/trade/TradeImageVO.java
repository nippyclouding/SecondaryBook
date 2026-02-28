package project.trade;

import lombok.Data;
import java.io.Serializable;

@Data
public class TradeImageVO implements Serializable {
    private long book_img_seq;
    private long trade_seq;
    private String img_url;
    private int sort_seq;
}
