package project.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import project.member.ENUM.MemberStatus;
import project.member.validation.SignUpGroup;
import project.member.validation.UpdateGroup;
import project.util.Const;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;


@Data
public class MemberVO implements Serializable {

    private static final long serialVersionUID = 1L;
    private long member_seq;

    @NotBlank(message = "아이디를 입력해주세요.", groups = {SignUpGroup.class})
    @Size(min = 4, max = 20, message = "아이디는 4~20자이어야 합니다.", groups = {SignUpGroup.class})
    private String login_id;

    @JsonIgnore
    @NotBlank(message = "비밀번호를 입력해주세요.", groups = {SignUpGroup.class})
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자이어야 합니다.", groups = {SignUpGroup.class})
    private String member_pwd;

    @NotBlank(message = "이메일을 입력해주세요.", groups = {SignUpGroup.class})
    @Email(message = "이메일 형식이 올바르지 않습니다.", groups = {SignUpGroup.class})
    private String member_email;

    @Pattern(regexp = "^(\\d{3}-\\d{4}-\\d{4})?$", message = "휴대폰 번호 형식이 올바르지 않습니다. (예: 010-1234-5678)",
            groups = {SignUpGroup.class, UpdateGroup.class})
    private String member_tel_no;

    @NotBlank(message = "닉네임을 입력해주세요.", groups = {SignUpGroup.class, UpdateGroup.class})
    @Size(min = 2, max = 20, message = "닉네임은 2~20자이어야 합니다.", groups = {SignUpGroup.class, UpdateGroup.class})
    private String member_nicknm;
    private LocalDateTime member_deleted_dtm;
    private LocalDateTime member_last_login_dtm;
    private MemberStatus member_st;
    private LocalDateTime upd_dtm;

    @JsonIgnore  // JSON 변환 시 제외
    private LocalDateTime crt_dtm;

    // JSON으로 반환할 포맷팅된 문자열
    @JsonProperty("crt_dtm")
    public String getCrtDtmFormatted() {
        return crt_dtm != null ? crt_dtm.format(Const.DATETIME_FORMATTER) : null;
    }
}
