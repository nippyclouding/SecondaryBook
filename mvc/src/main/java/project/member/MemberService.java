package project.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.bookclub.BookClubMapper;
import project.bookclub.BookClubService;
import project.bookclub.vo.BookClubVO;
import project.trade.TradeService;
import project.util.exception.InvalidRequestException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberMapper memberMapper;
    private final BookClubMapper bookClubMapper;
    private final BookClubService bookClubService;
    private final TradeService tradeService;
    private final BCryptPasswordEncoder passwordEncoder;

    // 회원 가입 - BCrypt 인코딩 후 저장
    @Transactional
    public boolean signUp(MemberVO vo) {
        vo.setMember_pwd(passwordEncoder.encode(vo.getMember_pwd()));
        return memberMapper.save(vo) > 0;
    }

    // 로그인 - BCrypt 우선, MD5 fallback + 자동 마이그레이션
    @Transactional
    public MemberVO login(MemberVO vo) {
        MemberVO member = memberMapper.findByLoginId(vo.getLogin_id());
        if (member == null) return null;

        String rawPassword = vo.getMember_pwd();
        String storedHash = member.getMember_pwd();

        // BCrypt 해시인 경우 ($2a$ 또는 $2b$로 시작)
        if (storedHash != null && (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$"))) {
            return passwordEncoder.matches(rawPassword, storedHash) ? member : null;
        }

        // MD5 해시인 경우 (기존 회원) → MD5로 검증 후 BCrypt로 마이그레이션
        MemberVO md5Result = memberMapper.findByMd5Password(vo);
        if (md5Result != null) {
            String bcryptHash = passwordEncoder.encode(rawPassword);
            memberMapper.updatePassword(vo.getLogin_id(), bcryptHash);
            log.info("MD5→BCrypt 마이그레이션 완료: {}", vo.getLogin_id());
            return md5Result;
        }

        return null;
    }

    // 마지막 로그인 시각 갱신
    @Transactional
    public boolean updateLastLogin(long member_seq) {
        return memberMapper.updateLastLogin(member_seq) > 0;
    }

    // ID 중복 개수 조회
    public int countByLoginId(String login_id) {
        return memberMapper.countByLoginId(login_id);
    }

    // 닉네임 중복 개수 조회
    public int countByNickname(String member_nicknm) {
        return memberMapper.countByNickname(member_nicknm);
    }

    // 이메일 중복 개수 조회
    public int countByEmail(String member_email) {
        return memberMapper.countByEmail(member_email);
    }

    // 2개 테이블 insert 보호
    @Transactional
    public MemberVO processSocialLogin(Map<String, Object> params) {
        // 1. 소셜 ID로 가입 여부 확인
        MemberVO existMember = memberMapper.findByOAuth(params);

        if (existMember != null) {
            // 이미 가입됨 -> 로그인
            if (existMember.getMember_deleted_dtm() != null) {
                return null;
            }
            return existMember;
        }

        // 2. 미가입 -> 회원가입 진행
        String nickname = (String) params.get("nickname"); // 소셜 로그인 시 중복체크
        while (memberMapper.countByNickname(nickname) > 0) {
            // 홍길동 --> 홍길동_1242(난수 4자리)
            int randomNum = (int) (Math.random() * 9000) + 1000;
            nickname = nickname + "_" + randomNum;
        }

        params.put("nickname", nickname);

        // (1) MEMBER_INFO 저장 (Map 사용)
        String generatedLoginId = params.get("provider") + "_" + params.get("provider_id");
        params.put("login_id", generatedLoginId);
        String generatedPwd = params.get("provider") + "_로그인";
        params.put("member_pwd", passwordEncoder.encode(generatedPwd));
        memberMapper.saveSocialMember(params);

        // (2) MEMBER_OAUTH 저장 (방금 생성된 member_seq 사용)
        // saveSocialMember 실행 시 params에 member_seq가 담겨옴
        memberMapper.saveMemberOAuth(params);

        // 3. 가입된 정보 다시 조회해서 리턴
        return memberMapper.findByOAuth(params);
    }

    // 프로필 - 회원 정보 수정
    @Transactional
    public boolean updateMember(MemberVO vo) {
        if (memberMapper.countByNicknameExcluding(vo.getMember_nicknm(), vo.getMember_seq()) > 0) {
            return false; // 다른 회원이 이미 사용 중인 닉네임
        }
        return memberMapper.updateMember(vo) > 0;
    }
    // 프로필 - 회원 탈퇴

    @Transactional
    public boolean deleteMember(long member_seq) {

        // 0. 결제 진행 중 또는 완료 후 구매 미확정 거래가 있으면 탈퇴 차단
        if (tradeService.hasActivePaymentsByMember(member_seq)) {
            throw new InvalidRequestException(
                "결제 진행 중이거나 구매 확정이 완료되지 않은 거래가 있어 탈퇴할 수 없습니다. " +
                "마이페이지 > 판매 내역에서 진행 중인 거래를 먼저 완료해 주세요."
            );
        }

        // 1. 탈퇴하는 회원이 쓴 Trade soft delete
        tradeService.deleteAllByMember(member_seq);

        // 2. 탈퇴한 회원의 Book Club 처리
        // BookClubVO seq 조회를 위한 select 쿼리 전달
        List<BookClubVO> bookClubVOS = bookClubMapper.selectMyBookClubs(member_seq);
        for (BookClubVO bookClubVO : bookClubVOS) {
            bookClubService.leaveBookClub(bookClubVO.getBook_club_seq(), member_seq);
        }

        return memberMapper.deleteMember(member_seq) > 0;
    }

    // 아이디 찾기 (전화번호로)
    public String findIdByTel(String member_tel_no) {
        return memberMapper.findIdByTel(member_tel_no);
    }

    // 비밀번호 찾기 - 회원 확인
    public boolean checkUserByIdAndEmail(String login_id, String member_email) {
        int count = memberMapper.countByLoginIdAndEmail(login_id, member_email);
        return count > 0;
    }

    // 비밀번호 재설정 - BCrypt 적용
    @Transactional
    public String resetPassword(String login_id, String new_pwd) {
        MemberVO member = memberMapper.findByLoginId(login_id);
        if (member == null) return "fail";

        String storedHash = member.getMember_pwd();

        // 이전 비밀번호와 같은지 확인
        if (storedHash != null && (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$"))) {
            if (passwordEncoder.matches(new_pwd, storedHash)) {
                return "same_password";
            }
        }

        // BCrypt로 인코딩 후 저장
        String bcryptHash = passwordEncoder.encode(new_pwd);
        int updateCount = memberMapper.updatePassword(login_id, bcryptHash);
        return updateCount > 0 ? "success" : "fail";
    }
}