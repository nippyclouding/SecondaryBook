package spring.batch.member;

import lombok.Data;

@Data
public class MemberVO {
    private long member_seq;
    private String login_id;
    private String member_pwd;
    private String member_nicknm;
    private String member_email;
    private String member_tel_no;
}
