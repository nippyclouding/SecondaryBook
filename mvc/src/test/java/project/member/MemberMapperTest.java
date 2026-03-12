package project.member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import project.config.TestMapperConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MemberMapper 통합 테스트
 * - H2 인메모리 DB + 실제 MyBatis XML 쿼리 실행
 * - @Transactional: 각 테스트 후 자동 롤백 → 테스트 간 독립성 보장
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestMapperConfig.class)
@Transactional
@DisplayName("MemberMapper - 실제 SQL 실행 (H2)")
class MemberMapperTest {

    @Autowired
    MemberMapper memberMapper;

    // ========== 헬퍼 ==========

    private MemberVO insertMember(String loginId, String email, String nickname) {
        MemberVO vo = new MemberVO();
        vo.setLogin_id(loginId);
        vo.setMember_pwd("$2a$10$dummyHashedPassword");
        vo.setMember_email(email);
        vo.setMember_nicknm(nickname);
        memberMapper.signUp(vo);
        return vo;
    }

    // ========================================================================
    // signUp - 회원 INSERT
    // ========================================================================
    @Nested
    @DisplayName("signUp - 회원 INSERT")
    class SignUp {

        @Test
        @DisplayName("정상 가입 - 반환값 1")
        void signUp_returns1() {
            MemberVO vo = new MemberVO();
            vo.setLogin_id("signupUser");
            vo.setMember_pwd("$2a$10$hashed");
            vo.setMember_email("signup@test.com");
            vo.setMember_nicknm("신규회원");

            int result = memberMapper.signUp(vo);

            assertThat(result).isEqualTo(1);
        }
    }

    // ========================================================================
    // findByLoginId - ID로 회원 조회
    // ========================================================================
    @Nested
    @DisplayName("findByLoginId - ID로 회원 조회")
    class FindByLoginId {

        @Test
        @DisplayName("존재하는 활성 회원 - VO 반환")
        void found_returnsVO() {
            insertMember("findUser", "find@test.com", "조회회원");

            MemberVO result = memberMapper.findByLoginId("findUser");

            assertThat(result).isNotNull();
            assertThat(result.getLogin_id()).isEqualTo("findUser");
            assertThat(result.getMember_nicknm()).isEqualTo("조회회원");
        }

        @Test
        @DisplayName("존재하지 않는 ID - null 반환")
        void notFound_returnsNull() {
            MemberVO result = memberMapper.findByLoginId("noSuchUser");
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("탈퇴한 회원(member_deleted_dtm 설정) - null 반환")
        void deletedMember_returnsNull() {
            insertMember("deletedUser", "deleted@test.com", "탈퇴회원");
            memberMapper.deleteMember(memberMapper.findByLoginId("deletedUser").getMember_seq());

            MemberVO result = memberMapper.findByLoginId("deletedUser");
            assertThat(result).isNull();
        }
    }

    // ========================================================================
    // idCheck / emailCheck / nickNmCheck - 중복 체크
    // ========================================================================
    @Nested
    @DisplayName("중복 체크 (idCheck / emailCheck / nickNmCheck)")
    class DuplicateCheck {

        @BeforeEach
        void insertTestMember() {
            insertMember("checkUser", "check@test.com", "체크닉");
        }

        @Test
        @DisplayName("idCheck - 존재하는 ID → 1")
        void idCheck_exists() {
            assertThat(memberMapper.idCheck("checkUser")).isEqualTo(1);
        }

        @Test
        @DisplayName("idCheck - 없는 ID → 0")
        void idCheck_notExists() {
            assertThat(memberMapper.idCheck("unknown")).isEqualTo(0);
        }

        @Test
        @DisplayName("emailCheck - 존재하는 이메일 → 1")
        void emailCheck_exists() {
            assertThat(memberMapper.emailCheck("check@test.com")).isEqualTo(1);
        }

        @Test
        @DisplayName("emailCheck - 없는 이메일 → 0")
        void emailCheck_notExists() {
            assertThat(memberMapper.emailCheck("none@test.com")).isEqualTo(0);
        }

        @Test
        @DisplayName("nickNmCheck - 존재하는 닉네임 → 1")
        void nickNmCheck_exists() {
            assertThat(memberMapper.nickNmCheck("체크닉")).isEqualTo(1);
        }

        @Test
        @DisplayName("nickNmCheck - 없는 닉네임 → 0")
        void nickNmCheck_notExists() {
            assertThat(memberMapper.nickNmCheck("없는닉")).isEqualTo(0);
        }
    }

    // ========================================================================
    // updatePassword - 비밀번호 변경
    // ========================================================================
    @Nested
    @DisplayName("updatePassword - 비밀번호 변경")
    class UpdatePassword {

        @Test
        @DisplayName("정상 변경 - 반환값 1 + 비밀번호 실제 변경")
        void update_succeeds() {
            insertMember("pwdUser", "pwd@test.com", "비번회원");
            String newPwd = "$2a$10$newHashedPassword";

            int result = memberMapper.updatePassword("pwdUser", newPwd);

            assertThat(result).isEqualTo(1);
            // 변경된 비밀번호 확인은 직접 DB 조회로 (findByLoginId는 비밀번호를 노출하지 않으나 필드는 갖고 있음)
            MemberVO found = memberMapper.findByLoginId("pwdUser");
            assertThat(found.getMember_pwd()).isEqualTo(newPwd);
        }

        @Test
        @DisplayName("없는 ID - 반환값 0")
        void notFound_returns0() {
            int result = memberMapper.updatePassword("noUser", "$2a$10$hash");
            assertThat(result).isEqualTo(0);
        }
    }

    // ========================================================================
    // updateMember - 회원 정보 수정
    // ========================================================================
    @Test
    @DisplayName("updateMember - 닉네임·전화번호 수정 성공")
    void updateMember_success() {
        insertMember("updateUser", "upd@test.com", "기존닉");
        long seq = memberMapper.findByLoginId("updateUser").getMember_seq();

        MemberVO updated = new MemberVO();
        updated.setMember_seq(seq);
        updated.setMember_nicknm("새닉네임");
        updated.setMember_tel_no("01099998888");

        int result = memberMapper.updateMember(updated);

        assertThat(result).isEqualTo(1);
        MemberVO found = memberMapper.findByLoginId("updateUser");
        assertThat(found.getMember_nicknm()).isEqualTo("새닉네임");
    }

    // ========================================================================
    // deleteMember - 소프트 삭제
    // ========================================================================
    @Test
    @DisplayName("deleteMember - member_deleted_dtm 설정 + 닉네임 '탈퇴한 회원'으로 변경")
    void deleteMember_setsDeletedDtm() {
        insertMember("deleteUser", "del@test.com", "탈퇴예정");
        long seq = memberMapper.findByLoginId("deleteUser").getMember_seq();

        int result = memberMapper.deleteMember(seq);

        assertThat(result).isEqualTo(1);
        // findByLoginId는 탈퇴 회원을 반환하지 않음 (WHERE MEMBER_DELETED_DTM IS NULL)
        MemberVO afterDelete = memberMapper.findByLoginId("deleteUser");
        assertThat(afterDelete).isNull();
    }

    // ========================================================================
    // findIdByTel - 전화번호로 아이디 찾기
    // ========================================================================
    @Nested
    @DisplayName("findIdByTel - 전화번호로 아이디 찾기")
    class FindIdByTel {

        @Test
        @DisplayName("일치하는 전화번호 존재 - 아이디 반환")
        void found_returnsLoginId() {
            insertMember("telUser", "tel@test.com", "전화회원");
            long seq = memberMapper.findByLoginId("telUser").getMember_seq();

            MemberVO vo = new MemberVO();
            vo.setMember_seq(seq);
            vo.setMember_nicknm("전화회원");
            vo.setMember_tel_no("01011112222");
            memberMapper.updateMember(vo);

            String result = memberMapper.findIdByTel("01011112222");
            assertThat(result).isEqualTo("telUser");
        }

        @Test
        @DisplayName("전화번호 없음 - null 반환")
        void notFound_returnsNull() {
            String result = memberMapper.findIdByTel("01000000000");
            assertThat(result).isNull();
        }
    }

    // ========================================================================
    // checkUserByIdAndEmail - ID + 이메일 매칭
    // ========================================================================
    @Nested
    @DisplayName("checkUserByIdAndEmail - ID + 이메일 매칭")
    class CheckUserByIdAndEmail {

        @Test
        @DisplayName("ID와 이메일 모두 일치 - 1 반환")
        void match_returns1() {
            insertMember("matchUser", "match@test.com", "매칭회원");

            int result = memberMapper.checkUserByIdAndEmail("matchUser", "match@test.com");
            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("이메일 불일치 - 0 반환")
        void emailMismatch_returns0() {
            insertMember("mismatch", "correct@test.com", "불일치회원");

            int result = memberMapper.checkUserByIdAndEmail("mismatch", "wrong@test.com");
            assertThat(result).isEqualTo(0);
        }
    }

    // ========================================================================
    // loginLogUpdate - 마지막 로그인 시간 갱신
    // ========================================================================
    @Test
    @DisplayName("loginLogUpdate - 반환값 1 (last_login_dtm 갱신)")
    void loginLogUpdate_returns1() {
        insertMember("logUser", "log@test.com", "로그회원");
        long seq = memberMapper.findByLoginId("logUser").getMember_seq();

        int result = memberMapper.loginLogUpdate(seq);

        assertThat(result).isEqualTo(1);
    }
}
