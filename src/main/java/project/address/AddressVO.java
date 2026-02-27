package project.address;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class AddressVO {
    private long addr_seq;
    private long member_seq;

    @NotBlank(message = "우편번호를 입력해주세요.")
    @Pattern(regexp = "\\d{5}", message = "우편번호는 5자리 숫자여야 합니다.")
    private String post_no;

    @NotBlank(message = "기본 주소를 입력해주세요.")
    private String addr_h;

    @NotBlank(message = "상세 주소를 입력해주세요.")
    private String addr_d;

    private int default_yn;

    @NotBlank(message = "주소명을 입력해주세요.")
    private String addr_nm;
}