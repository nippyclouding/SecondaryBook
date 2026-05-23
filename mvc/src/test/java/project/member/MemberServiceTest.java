package project.member;

import org.junit.jupiter.api.DisplayName;
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
import project.util.exception.InvalidRequestException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService - 인증 및 회원 생명주기")
class MemberServiceTest {

    @Mock MemberMapper memberMapper;
    @Mock BookClubMapper bookClubMapper;
    @Mock BookClubService bookClubService;
    @Mock TradeService tradeService;
    @Mock BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    MemberService memberService;

    @Test
    @DisplayName("회원가입 비밀번호는 암호화한 값만 저장한다")
    void signUpStoresHashedPassword() {
        MemberVO member = member("testUser", "rawPassword123");
        when(passwordEncoder.encode("rawPassword123")).thenReturn("$2a$10$hashedValue");
        when(memberMapper.save(member)).thenReturn(1);

        assertThat(memberService.signUp(member)).isTrue();
        assertThat(member.getMember_pwd()).isEqualTo("$2a$10$hashedValue");
        verify(memberMapper).save(member);
    }

    @Test
    @DisplayName("BCrypt 계정은 비밀번호 일치 시 로그인한다")
    void bcryptLoginUsesPasswordEncoder() {
        MemberVO input = member("testUser", "rawPassword");
        MemberVO saved = member("testUser", "$2b$10$hashedValue");
        when(memberMapper.findByLoginId("testUser")).thenReturn(saved);
        when(passwordEncoder.matches("rawPassword", "$2b$10$hashedValue")).thenReturn(true);

        assertThat(memberService.login(input)).isSameAs(saved);
    }

    @Test
    @DisplayName("기존 MD5 계정의 정상 로그인은 BCrypt 비밀번호로 마이그레이션한다")
    void md5LoginMigratesHash() {
        MemberVO input = member("oldUser", "rawPassword");
        MemberVO saved = member("oldUser", "e99a18c428cb38d5f260853678922e03");
        when(memberMapper.findByLoginId("oldUser")).thenReturn(saved);
        when(memberMapper.findByMd5Password(input)).thenReturn(saved);
        when(passwordEncoder.encode("rawPassword")).thenReturn("$2a$10$newBcryptHash");

        assertThat(memberService.login(input)).isSameAs(saved);
        verify(memberMapper).updatePassword("oldUser", "$2a$10$newBcryptHash");
    }

    @Test
    @DisplayName("비밀번호 재설정은 기존 비밀번호 재사용을 거부한다")
    void resetPasswordRejectsSamePassword() {
        MemberVO saved = member("testUser", "$2a$10$oldHash");
        when(memberMapper.findByLoginId("testUser")).thenReturn(saved);
        when(passwordEncoder.matches("samePassword", "$2a$10$oldHash")).thenReturn(true);

        assertThat(memberService.resetPassword("testUser", "samePassword")).isEqualTo("same_password");
        verify(memberMapper, never()).updatePassword(any(), any());
    }

    @Test
    @DisplayName("비밀번호 재설정 성공 시 새 해시를 저장한다")
    void resetPasswordStoresNewHash() {
        MemberVO saved = member("testUser", "$2a$10$oldHash");
        when(memberMapper.findByLoginId("testUser")).thenReturn(saved);
        when(passwordEncoder.matches("newPassword", "$2a$10$oldHash")).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("$2a$10$newHash");
        when(memberMapper.updatePassword("testUser", "$2a$10$newHash")).thenReturn(1);

        assertThat(memberService.resetPassword("testUser", "newPassword")).isEqualTo("success");
        verify(memberMapper).updatePassword("testUser", "$2a$10$newHash");
    }

    @Test
    @DisplayName("탈퇴한 소셜 계정은 다시 로그인하지 못한다")
    void deletedSocialMemberCannotLogin() {
        Map<String, Object> params = socialParams();
        MemberVO deleted = member("kakao_12345", "$2a$10$hash");
        deleted.setMember_deleted_dtm(LocalDateTime.now());
        when(memberMapper.findByOAuth(params)).thenReturn(deleted);

        assertThat(memberService.processSocialLogin(params)).isNull();
    }

    @Test
    @DisplayName("신규 소셜 로그인은 회원과 OAuth 연결을 함께 생성한다")
    void socialLoginCreatesMemberAndOAuthLink() {
        Map<String, Object> params = socialParams();
        MemberVO created = member("KAKAO_12345", "$2a$10$hash");
        when(memberMapper.findByOAuth(params)).thenReturn(null).thenReturn(created);
        when(memberMapper.countByNickname("testNick")).thenReturn(0);
        when(passwordEncoder.encode("KAKAO_로그인")).thenReturn("$2a$10$socialHash");

        assertThat(memberService.processSocialLogin(params)).isSameAs(created);
        verify(memberMapper).saveSocialMember(params);
        verify(memberMapper).saveMemberOAuth(params);
    }

    @Test
    @DisplayName("진행 중 또는 미확정 결제가 있는 회원은 탈퇴시키지 않는다")
    void activePaymentBlocksDeletion() {
        when(tradeService.hasActivePaymentsByMember(1L)).thenReturn(true);

        assertThatThrownBy(() -> memberService.deleteMember(1L))
                .isInstanceOf(InvalidRequestException.class);
        verify(tradeService, never()).deleteAllByMember(1L);
        verify(memberMapper, never()).deleteMember(1L);
    }

    @Test
    @DisplayName("탈퇴 시 등록 거래와 가입 모임을 정리한 뒤 회원을 삭제한다")
    void deleteMemberCleansOwnedData() {
        BookClubVO club1 = new BookClubVO();
        club1.setBook_club_seq(10L);
        BookClubVO club2 = new BookClubVO();
        club2.setBook_club_seq(20L);
        when(bookClubMapper.selectMyBookClubs(1L)).thenReturn(Arrays.asList(club1, club2));
        when(memberMapper.deleteMember(1L)).thenReturn(1);

        assertThat(memberService.deleteMember(1L)).isTrue();
        verify(tradeService).deleteAllByMember(1L);
        verify(bookClubService).leaveBookClub(10L, 1L);
        verify(bookClubService).leaveBookClub(20L, 1L);
    }

    private MemberVO member(String loginId, String password) {
        MemberVO member = new MemberVO();
        member.setLogin_id(loginId);
        member.setMember_pwd(password);
        return member;
    }

    private Map<String, Object> socialParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("provider", "KAKAO");
        params.put("provider_id", "12345");
        params.put("nickname", "testNick");
        return params;
    }
}
