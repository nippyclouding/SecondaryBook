package project.member;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface MemberMapper {
    MemberVO findByLoginId(@Param("login_id") String login_id);
    MemberVO loginByMd5(MemberVO vo);
    int loginLogUpdate(@Param("member_seq") long member_seq);
    int signUp(MemberVO vo);

    //
    int idCheck(@Param("login_id") String login_id);
    int nickNmCheck(@Param("member_nicknm") String member_nicknm);
    int emailCheck(@Param("member_email") String member_email);

    MemberVO getMemberByOAuth(Map<String, Object> map);
    void insertSocialMemberInfo(Map<String, Object> map);
    void insertMemberOAuth(Map<String, Object> map);

    int updateMember(MemberVO vo);
    int deleteMember(@Param("member_seq") long member_seq);

    String findIdByTel(@Param("member_tel_no") String member_tel_no);

    int checkUserByIdAndEmail(@Param("login_id") String login_id,
                              @Param("member_email") String member_email);

    int updatePassword(@Param("login_id") String login_id,
                       @Param("member_pwd") String member_pwd);

    MemberVO findByMemberSeq(@Param("member_seq") long member_seq);

}
