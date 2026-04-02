package project.member;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class MemberBankAccountVO implements Serializable {
    private long bank_account_seq;
    private long member_seq;
    private String bank_code;
    private String bank_account_no;
    private String account_holder_nm;
    private boolean verified_yn;
    private LocalDateTime crt_dtm;
    private LocalDateTime upd_dtm;
}
