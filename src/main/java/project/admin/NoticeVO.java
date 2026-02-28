package project.admin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.io.Serializable;
import project.util.Const;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;


@Data
public class NoticeVO implements Serializable {
    private Long notice_seq;
    private Long admin_seq;

    @NotBlank(message = "공지사항 제목을 입력해주세요.")
    @Size(max = 200, message = "제목은 200자 이내로 입력해주세요.")
    private String notice_title;

    private int notice_priority;
    private boolean active;

    @NotBlank(message = "공지사항 내용을 입력해주세요.")
    private String notice_cont;
    private LocalDateTime upd_dtm;

    private String admin_login_id;
    private Long view_count;

    @JsonIgnore  // JSON 변환 시 제외
    private LocalDateTime crt_dtm;

    // JSON으로 반환할 포맷팅된 문자열
    @JsonProperty("crt_dtm")
    public String getCrtDtmFormatted() {
        return crt_dtm != null ? crt_dtm.format(Const.DATETIME_FORMATTER) : null;
    }
}
