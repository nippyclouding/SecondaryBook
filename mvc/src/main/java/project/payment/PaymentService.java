package project.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.address.AddressMapper;
import project.address.AddressVO;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {

    private final AddressMapper addressMapper;
    @Transactional(readOnly = true)
    public List<AddressVO> findAddress(Long member_seller_seq) {
        return addressMapper.selectAddressList(member_seller_seq);
    }

}
