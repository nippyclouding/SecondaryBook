package project.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import project.bookclub.BookClubMapper;
import project.bookclub.BookClubService;
import project.bookclub.vo.BookClubVO;
import project.trade.TradeService;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    MemberMapper memberMapper;

    @Mock
    BookClubMapper bookClubMapper;

    @Mock
    BookClubService bookClubService;

    @Mock
    TradeService tradeService;

    @Mock
    BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    MemberService memberService;

    // ========== 헬퍼 메서드 ==========

    private MemberVO createMember(String loginId, String pwd) {
        MemberVO member = new MemberVO();
        member.setLogin_id(loginId);
        member.setMember_pwd(pwd);
        return member;
    }

    // ========================================================================
    // signUp - 회원가입
    // ========================================================================
    @Nested
    @DisplayName("signUp - 회원가입")
    class SignUp {

        @Test
        @DisplayName("비밀번호가 BCrypt로 암호화되어 저장된다")
        void 비밀번호_암호화_저장() {
            // given
            MemberVO vo = createMember("testUser", "rawPassword123");
            when(passwordEncoder.encode("rawPassword123")).thenReturn("$2a$10$hashedValue");
            when(memberMapper.signUp(vo)).thenReturn(1);

            // when
            boolean result = memberService.signUp(vo);

            // then
            assertThat(result).isTrue();
            assertThat(vo.getMember_pwd()).isEqualTo("$2a$10$hashedValue");
            verify(passwordEncoder).encode("rawPassword123");
            verify(memberMapper).signUp(vo);
        }

        @Test
        @DisplayName("가입 실패 시 false 반환")
        void 가입실패_false() {
            // given
            MemberVO vo = createMember("testUser", "rawPassword");
            when(passwordEncoder.encode("rawPassword")).thenReturn("$2a$10$hashed");
            when(memberMapper.signUp(vo)).thenReturn(0);

            // when
            boolean result = memberService.signUp(vo);

            // then
            assertThat(result).isFalse();
        }
    }

    // ========================================================================
    // login - 로그인
    // ========================================================================
    @Nested
    @DisplayName("login - 로그인")
    class Login {

        @Test
        @DisplayName("BCrypt 회원 - 정상 로그인")
        void BCrypt회원_로그인성공() {
            // given
            MemberVO input = createMember("testUser", "rawPassword");
            MemberVO dbMember = createMember("testUser", "$2a$10$hashedValue");

            when(memberMapper.findByLoginId("testUser")).thenReturn(dbMember);
            when(passwordEncoder.matches("rawPassword", "$2a$10$hashedValue")).thenReturn(true);

            // when
            MemberVO result = memberService.login(input);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getLogin_id()).isEqualTo("testUser");
        }

        @Test
        @DisplayName("BCrypt($2b$) 회원 - 정상 로그인")
        void BCrypt_2b_회원_로그인성공() {
            // given
            MemberVO input = createMember("testUser", "rawPassword");
            MemberVO dbMember = createMember("testUser", "$2b$10$hashedValue");

            when(memberMapper.findByLoginId("testUser")).thenReturn(dbMember);
            when(passwordEncoder.matches("rawPassword", "$2b$10$hashedValue")).thenReturn(true);

            // when
            MemberVO result = memberService.login(input);

            // then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("BCrypt 회원 - 비밀번호 불일치")
        void BCrypt회원_비밀번호불일치() {
            // given
            MemberVO input = createMember("testUser", "wrongPassword");
            MemberVO dbMember = createMember("testUser", "$2a$10$hashedValue");

            when(memberMapper.findByLoginId("testUser")).thenReturn(dbMember);
            when(passwordEncoder.matches("wrongPassword", "$2a$10$hashedValue")).thenReturn(false);

            // when
            MemberVO result = memberService.login(input);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 ID - null 반환")
        void 존재하지않는ID_null() {
            // given
            MemberVO input = createMember("noUser", "password");
            when(memberMapper.findByLoginId("noUser")).thenReturn(null);

            // when
            MemberVO result = memberService.login(input);

            // then
            assertThat(result).isNull();
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("MD5 회원 - 로그인 성공 + BCrypt 자동 마이그레이션")
        void MD5회원_로그인_마이그레이션() {
            // given
            MemberVO input = createMember("oldUser", "rawPassword");
            // DB에 저장된 해시가 MD5 (BCrypt 형식이 아님)
            MemberVO dbMember = createMember("oldUser", "e99a18c428cb38d5f260853678922e03");

            when(memberMapper.findByLoginId("oldUser")).thenReturn(dbMember);
            when(memberMapper.loginByMd5(input)).thenReturn(dbMember);
            when(passwordEncoder.encode("rawPassword")).thenReturn("$2a$10$newBcryptHash");

            // when
            MemberVO result = memberService.login(input);

            // then
            assertThat(result).isNotNull();
            verify(passwordEncoder).encode("rawPassword");
            verify(memberMapper).updatePassword("oldUser", "$2a$10$newBcryptHash");
        }

        @Test
        @DisplayName("MD5 회원 - 비밀번호 불일치")
        void MD5회원_비밀번호불일치() {
            // given
            MemberVO input = createMember("oldUser", "wrongPassword");
            MemberVO dbMember = createMember("oldUser", "e99a18c428cb38d5f260853678922e03");

            when(memberMapper.findByLoginId("oldUser")).thenReturn(dbMember);
            when(memberMapper.loginByMd5(input)).thenReturn(null);

            // when
            MemberVO result = memberService.login(input);

            // then
            assertThat(result).isNull();
            verify(memberMapper, never()).updatePassword(anyString(), anyString());
        }
    }

    // ========================================================================
    // 중복 체크 (ID, 닉네임, 이메일)
    // ========================================================================
    @Nested
    @DisplayName("중복 체크")
    class DuplicateCheck {

        @Test
        @DisplayName("ID 중복 - 1 반환")
        void idCheck_중복() {
            when(memberMapper.idCheck("existUser")).thenReturn(1);
            assertThat(memberService.idCheck("existUser")).isEqualTo(1);
        }

        @Test
        @DisplayName("ID 사용 가능 - 0 반환")
        void idCheck_사용가능() {
            when(memberMapper.idCheck("newUser")).thenReturn(0);
            assertThat(memberService.idCheck("newUser")).isEqualTo(0);
        }

        @Test
        @DisplayName("닉네임 중복 - 1 반환")
        void nickNmCheck_중복() {
            when(memberMapper.nickNmCheck("existNick")).thenReturn(1);
            assertThat(memberService.nickNmCheck("existNick")).isEqualTo(1);
        }

        @Test
        @DisplayName("이메일 중복 - 1 반환")
        void emailCheck_중복() {
            when(memberMapper.emailCheck("exist@test.com")).thenReturn(1);
            assertThat(memberService.emailCheck("exist@test.com")).isEqualTo(1);
        }
    }

    // ========================================================================
    // resetPassword - 비밀번호 재설정
    // ========================================================================
    @Nested
    @DisplayName("resetPassword - 비밀번호 재설정")
    class ResetPassword {

        @Test
        @DisplayName("정상 재설정 - success 반환")
        void 정상재설정_success() {
            // given
            MemberVO dbMember = createMember("testUser", "$2a$10$oldHash");
            when(memberMapper.findByLoginId("testUser")).thenReturn(dbMember);
            when(passwordEncoder.matches("newPassword", "$2a$10$oldHash")).thenReturn(false);
            when(passwordEncoder.encode("newPassword")).thenReturn("$2a$10$newHash");
            when(memberMapper.updatePassword("testUser", "$2a$10$newHash")).thenReturn(1);

            // when
            String result = memberService.resetPassword("testUser", "newPassword");

            // then
            assertThat(result).isEqualTo("success");
            verify(memberMapper).updatePassword("testUser", "$2a$10$newHash");
        }

        @Test
        @DisplayName("이전 비밀번호와 동일 - same_password 반환")
        void 이전비밀번호동일_samePassword() {
            // given
            MemberVO dbMember = createMember("testUser", "$2a$10$oldHash");
            when(memberMapper.findByLoginId("testUser")).thenReturn(dbMember);
            when(passwordEncoder.matches("samePassword", "$2a$10$oldHash")).thenReturn(true);

            // when
            String result = memberService.resetPassword("testUser", "samePassword");

            // then
            assertThat(result).isEqualTo("same_password");
            verify(memberMapper, never()).updatePassword(anyString(), anyString());
        }

        @Test
        @DisplayName("존재하지 않는 회원 - fail 반환")
        void 존재하지않는회원_fail() {
            // given
            when(memberMapper.findByLoginId("noUser")).thenReturn(null);

            // when
            String result = memberService.resetPassword("noUser", "newPassword");

            // then
            assertThat(result).isEqualTo("fail");
        }

        @Test
        @DisplayName("DB 업데이트 실패 - fail 반환")
        void DB업데이트실패_fail() {
            // given
            MemberVO dbMember = createMember("testUser", "$2a$10$oldHash");
            when(memberMapper.findByLoginId("testUser")).thenReturn(dbMember);
            when(passwordEncoder.matches("newPwd", "$2a$10$oldHash")).thenReturn(false);
            when(passwordEncoder.encode("newPwd")).thenReturn("$2a$10$newHash");
            when(memberMapper.updatePassword("testUser", "$2a$10$newHash")).thenReturn(0);

            // when
            String result = memberService.resetPassword("testUser", "newPwd");

            // then
            assertThat(result).isEqualTo("fail");
        }
    }

    // ========================================================================
    // checkUserByIdAndEmail - 비밀번호 찾기 회원 확인
    // ========================================================================
    @Nested
    @DisplayName("checkUserByIdAndEmail - 회원 확인")
    class CheckUserByIdAndEmail {

        @Test
        @DisplayName("일치하는 회원 존재 - true")
        void 일치_true() {
            when(memberMapper.checkUserByIdAndEmail("testUser", "test@test.com")).thenReturn(1);

            boolean result = memberService.checkUserByIdAndEmail("testUser", "test@test.com");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("일치하는 회원 없음 - false")
        void 불일치_false() {
            when(memberMapper.checkUserByIdAndEmail("noUser", "no@test.com")).thenReturn(0);

            boolean result = memberService.checkUserByIdAndEmail("noUser", "no@test.com");

            assertThat(result).isFalse();
        }
    }

    // ========================================================================
    // processSocialLogin - 소셜 로그인
    // ========================================================================
    @Nested
    @DisplayName("processSocialLogin - 소셜 로그인")
    class ProcessSocialLogin {

        @Test
        @DisplayName("기존 소셜 회원 - 바로 로그인")
        void 기존회원_바로로그인() {
            // given
            Map<String, Object> params = new HashMap<>();
            params.put("provider", "kakao");
            params.put("provider_id", "12345");

            MemberVO existMember = createMember("kakao_12345", "$2a$10$hash");
            when(memberMapper.getMemberByOAuth(params)).thenReturn(existMember);

            // when
            MemberVO result = memberService.processSocialLogin(params);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getLogin_id()).isEqualTo("kakao_12345");
            verify(memberMapper, never()).insertSocialMemberInfo(any());
        }

        @Test
        @DisplayName("탈퇴한 소셜 회원 - null 반환")
        void 탈퇴한회원_null() {
            // given
            Map<String, Object> params = new HashMap<>();
            params.put("provider", "kakao");
            params.put("provider_id", "12345");

            MemberVO deletedMember = createMember("kakao_12345", "$2a$10$hash");
            deletedMember.setMember_deleted_dtm(LocalDateTime.now());
            when(memberMapper.getMemberByOAuth(params)).thenReturn(deletedMember);

            // when
            MemberVO result = memberService.processSocialLogin(params);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("신규 소셜 회원 - 가입 후 로그인")
        void 신규회원_가입후로그인() {
            // given
            Map<String, Object> params = new HashMap<>();
            params.put("provider", "kakao");
            params.put("provider_id", "12345");
            params.put("nickname", "testNick");

            MemberVO newMember = createMember("kakao_12345", "$2a$10$hash");

            // 첫 호출: 미가입, 두 번째 호출: 가입 완료 후 조회
            when(memberMapper.getMemberByOAuth(params))
                    .thenReturn(null)
                    .thenReturn(newMember);
            when(memberMapper.nickNmCheck("testNick")).thenReturn(0);
            when(passwordEncoder.encode("kakao_로그인")).thenReturn("$2a$10$socialHash");

            // when
            MemberVO result = memberService.processSocialLogin(params);

            // then
            assertThat(result).isNotNull();
            verify(memberMapper).insertSocialMemberInfo(params);
            verify(memberMapper).insertMemberOAuth(params);
        }

        @Test
        @DisplayName("닉네임 중복 시 난수 추가")
        void 닉네임중복_난수추가() {
            // given
            Map<String, Object> params = new HashMap<>();
            params.put("provider", "naver");
            params.put("provider_id", "67890");
            params.put("nickname", "duplicateNick");

            // 첫 번째 닉네임 중복, 두 번째 사용 가능
            when(memberMapper.getMemberByOAuth(params)).thenReturn(null).thenReturn(new MemberVO());
            when(memberMapper.nickNmCheck("duplicateNick")).thenReturn(1);
            when(memberMapper.nickNmCheck(argThat(nick -> nick.startsWith("duplicateNick_")))).thenReturn(0);
            when(passwordEncoder.encode("naver_로그인")).thenReturn("$2a$10$hash");

            // when
            MemberVO result = memberService.processSocialLogin(params);

            // then
            String finalNickname = (String) params.get("nickname");
            assertThat(finalNickname).startsWith("duplicateNick_");
            verify(memberMapper).insertSocialMemberInfo(params);
        }
    }

    // ========================================================================
    // deleteMember - 회원 탈퇴
    // ========================================================================
    @Nested
    @DisplayName("deleteMember - 회원 탈퇴")
    class DeleteMember {

        @Test
        @DisplayName("정상 탈퇴 - Trade 삭제 + BookClub 탈퇴 + 회원 삭제")
        void 정상탈퇴() {
            // given
            long memberSeq = 1L;

            // Trade 삭제
            when(tradeService.deleteAllByMember(memberSeq)).thenReturn(3);

            // BookClub 탈퇴
            BookClubVO club1 = new BookClubVO();
            club1.setBook_club_seq(10L);
            BookClubVO club2 = new BookClubVO();
            club2.setBook_club_seq(20L);
            when(bookClubMapper.selectMyBookClubs(memberSeq)).thenReturn(Arrays.asList(club1, club2));
            when(bookClubService.leaveBookClub(anyLong(), eq(memberSeq))).thenReturn(new HashMap<>());

            // 회원 삭제
            when(memberMapper.deleteMember(memberSeq)).thenReturn(1);

            // when
            boolean result = memberService.deleteMember(memberSeq);

            // then
            assertThat(result).isTrue();
            verify(tradeService).deleteAllByMember(memberSeq);
            verify(bookClubService).leaveBookClub(10L, memberSeq);
            verify(bookClubService).leaveBookClub(20L, memberSeq);
            verify(memberMapper).deleteMember(memberSeq);
        }

        @Test
        @DisplayName("가입한 모임이 없는 회원 탈퇴")
        void 모임없는회원_탈퇴() {
            // given
            long memberSeq = 1L;
            when(tradeService.deleteAllByMember(memberSeq)).thenReturn(0);
            when(bookClubMapper.selectMyBookClubs(memberSeq)).thenReturn(Collections.emptyList());
            when(memberMapper.deleteMember(memberSeq)).thenReturn(1);

            // when
            boolean result = memberService.deleteMember(memberSeq);

            // then
            assertThat(result).isTrue();
            verify(bookClubService, never()).leaveBookClub(anyLong(), anyLong());
        }
    }

    // ========================================================================
    // findIdByTel - 아이디 찾기
    // ========================================================================
    @Nested
    @DisplayName("findIdByTel - 아이디 찾기")
    class FindIdByTel {

        @Test
        @DisplayName("전화번호로 아이디 찾기 - 존재")
        void 아이디찾기_존재() {
            when(memberMapper.findIdByTel("010-1234-5678")).thenReturn("testUser");

            String result = memberService.findIdByTel("010-1234-5678");

            assertThat(result).isEqualTo("testUser");
        }

        @Test
        @DisplayName("전화번호로 아이디 찾기 - 미존재")
        void 아이디찾기_미존재() {
            when(memberMapper.findIdByTel("010-0000-0000")).thenReturn(null);

            String result = memberService.findIdByTel("010-0000-0000");

            assertThat(result).isNull();
        }
    }

    // ========================================================================
    // updateMember - 회원 정보 수정
    // ========================================================================
    @Nested
    @DisplayName("updateMember - 회원 정보 수정")
    class UpdateMember {

        @Test
        @DisplayName("정상 수정 - true 반환")
        void 정상수정_true() {
            MemberVO vo = new MemberVO();
            when(memberMapper.updateMember(vo)).thenReturn(1);

            assertThat(memberService.updateMember(vo)).isTrue();
        }

        @Test
        @DisplayName("수정 실패 - false 반환")
        void 수정실패_false() {
            MemberVO vo = new MemberVO();
            when(memberMapper.updateMember(vo)).thenReturn(0);

            assertThat(memberService.updateMember(vo)).isFalse();
        }
    }
}
