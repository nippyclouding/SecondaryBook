package project.member;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberBankAccountMapper {

    MemberBankAccountVO findByMemberSeq(@Param("member_seq") long member_seq);

    int insert(MemberBankAccountVO vo);

    int update(MemberBankAccountVO vo);
}
