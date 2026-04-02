package project.member;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface MemberMapper {
    MemberVO findByLoginId(@Param("login_id") String login_id);
    MemberVO findByMd5Password(MemberVO vo);
    int updateLastLogin(@Param("member_seq") long member_seq);
    int save(MemberVO vo);

    int countByLoginId(@Param("login_id") String login_id);
    int countByNickname(@Param("member_nicknm") String member_nicknm);
    int countByNicknameExcluding(@Param("member_nicknm") String member_nicknm, @Param("member_seq") long member_seq);
    int countByEmail(@Param("member_email") String member_email);

    MemberVO findByOAuth(Map<String, Object> map);
    void saveSocialMember(Map<String, Object> map);
    void saveMemberOAuth(Map<String, Object> map);

    int updateMember(MemberVO vo);
    int deleteMember(@Param("member_seq") long member_seq);

    String findIdByTel(@Param("member_tel_no") String member_tel_no);

    int countByLoginIdAndEmail(@Param("login_id") String login_id,
                               @Param("member_email") String member_email);

    int updatePassword(@Param("login_id") String login_id,
                       @Param("member_pwd") String member_pwd);

    MemberVO findByMemberSeq(@Param("member_seq") long member_seq);

}
