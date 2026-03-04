package project.admin;

import lombok.Data;
import java.io.Serializable;
import project.util.Const;

import java.time.LocalDateTime;


import static project.util.Const.*;

@Data
public class LoginInfoVO implements Serializable {
    private Long login_info_seq;
    private Long admin_seq;
    private Long member_seq;
    private LocalDateTime login_dtm;
    private LocalDateTime logout_dtm;
    private String login_ip;
    private String logout_ip;

    private String admin_login_id;

    private String member_nicknm;
    private String member_email;



    // ===== 접속 여부 =====
    public boolean isActive() {
        return logout_dtm == null;
    }

}
