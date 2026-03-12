package spring.batch.member;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {

    MemberVO findByMemberSeq(@Param("member_seq") long member_seq);
}
