package project.admin;

import lombok.Data;
import project.util.Const;

import java.time.LocalDateTime;


import static project.util.Const.*;

@Data
public class LoginInfoVO {
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



    // ===== 로그인 시간 포맷팅 =====
    public String getFormattedLoginDtm() {
        return login_dtm != null ? login_dtm.format(Const.DATETIME_FORMATTER) : "-";
    }

    // ===== 로그아웃 시간 포맷팅 =====
    public String getFormattedLogoutDtm() {
        return logout_dtm != null ? logout_dtm.format(Const.DATETIME_FORMATTER) : "-";
    }

    // ===== 접속 여부 =====
    public boolean isActive() {
        return logout_dtm == null;
    }

    // ===== 접속 시간 계산 (분 단위) =====
    public Long getSessionDurationMinutes() {
        if (login_dtm == null || logout_dtm == null) {
            return null;
        }
        return java.time.Duration.between(login_dtm, logout_dtm).toMinutes();
    }
}
