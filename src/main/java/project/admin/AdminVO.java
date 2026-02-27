package project.admin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
public class AdminVO implements Serializable {

    private static final long serialVersionUID = 1L;
    private long admin_seq;         // PK

    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(min = 5, max = 20, message = "아이디는 5~20자이어야 합니다.")
    private String admin_login_id;  // ID

    @JsonIgnore
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자이어야 합니다.")
    private String admin_password;  // Password
}