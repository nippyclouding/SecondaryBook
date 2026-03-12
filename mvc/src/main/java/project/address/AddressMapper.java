package project.address;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AddressMapper {
    List<AddressVO> selectAddressList(long member_seq);
    int insertAddress(AddressVO vo);
    int deleteAddress(@Param("addr_seq") long addr_seq, @Param("member_seq") long member_seq);
    int resetDefaultAddress(long member_seq);
    int setDefaultAddress(@Param("addr_seq") long addr_seq, @Param("member_seq") long member_seq);
    int updateAddress(AddressVO vo);
    int countAddress(long member_seq); // 주소 갯수 제한 (5개)
}