package project.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.util.AesEncryptionUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberBankAccountService {

    private final MemberBankAccountMapper memberBankAccountMapper;
    private final AesEncryptionUtil aesEncryptionUtil;

    /**
     * 계좌 조회 - 계좌번호를 복호화하여 반환한다.
     */
    public MemberBankAccountVO getByMemberSeq(long member_seq) {
        MemberBankAccountVO vo = memberBankAccountMapper.findByMemberSeq(member_seq);
        if (vo != null && vo.getBank_account_no() != null) {
            vo.setBank_account_no(aesEncryptionUtil.decrypt(vo.getBank_account_no()));
        }
        return vo;
    }

    /**
     * 계좌 등록/수정 - 계좌번호를 암호화하여 저장한다.
     */
    @Transactional
    public void save(MemberBankAccountVO vo) {
        vo.setBank_account_no(aesEncryptionUtil.encrypt(vo.getBank_account_no()));

        MemberBankAccountVO existing = memberBankAccountMapper.findByMemberSeq(vo.getMember_seq());
        if (existing == null) {
            memberBankAccountMapper.insert(vo);
            log.info("계좌 등록: member_seq={}, bank_code={}", vo.getMember_seq(), vo.getBank_code());
        } else {
            vo.setBank_account_seq(existing.getBank_account_seq());
            memberBankAccountMapper.update(vo);
            log.info("계좌 수정: member_seq={}, bank_code={}", vo.getMember_seq(), vo.getBank_code());
        }
    }
}
