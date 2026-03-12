package project.address;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {

    private final AddressMapper addressMapper;

    public List<AddressVO> getAddressList(long member_seq) {
        return addressMapper.selectAddressList(member_seq);
    }

    @Transactional
    public boolean addAddress(AddressVO vo) {
        if (vo.getDefault_yn() == 1) {
            addressMapper.resetDefaultAddress(vo.getMember_seq());
        }

        // 첫 배송지는 무조건 기본 배송지
        List<AddressVO> list = addressMapper.selectAddressList(vo.getMember_seq());
        if (list.isEmpty()) {
            vo.setDefault_yn(1);
        }

        return addressMapper.insertAddress(vo) > 0;
    }

    @Transactional
    public boolean updateAddress(AddressVO vo) {
        // 수정하면서 기본 배송지로 설정했을 경우
        if (vo.getDefault_yn() == 1) {
            addressMapper.resetDefaultAddress(vo.getMember_seq());
        }
        return addressMapper.updateAddress(vo) > 0;
    }

    @Transactional
    public boolean deleteAddress(long addr_seq, long member_seq) {
        return addressMapper.deleteAddress(addr_seq, member_seq) > 0;
    }

    @Transactional
    public boolean setMyDefaultAddress(long member_seq, long addr_seq) {
        addressMapper.resetDefaultAddress(member_seq);
        return addressMapper.setDefaultAddress(addr_seq, member_seq) > 0;
    }

    public int getAddressCount(long member_seq) {
        return addressMapper.countAddress(member_seq);
    }
}