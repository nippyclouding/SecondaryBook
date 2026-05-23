package project.member;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import project.member.validation.SignUpGroup;
import project.member.validation.UpdateGroup;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MemberVO - 입력 계약")
class MemberVoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator();
    }

    @Test
    @DisplayName("정상 회원가입 입력은 허용한다")
    void validSignUpAllowed() {
        assertThat(validator.validate(validSignUp(), SignUpGroup.class)).isEmpty();
    }

    @Test
    @DisplayName("회원가입 필수 형식 위반은 각 필드에서 거부한다")
    void invalidSignUpFieldsRejected() {
        MemberVO vo = validSignUp();
        vo.setLogin_id("abc");
        vo.setMember_pwd("short");
        vo.setMember_email("invalid-email");
        vo.setMember_nicknm("a");
        vo.setMember_tel_no("0101234ABCD");

        Set<String> invalidFields = validator.validate(vo, SignUpGroup.class).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertThat(invalidFields).contains(
                "login_id", "member_pwd", "member_email", "member_nicknm", "member_tel_no");
    }

    @Test
    @DisplayName("선택 입력인 전화번호는 생략할 수 있다")
    void optionalPhoneMayBeAbsent() {
        MemberVO vo = validSignUp();
        vo.setMember_tel_no(null);

        assertThat(validator.validate(vo, SignUpGroup.class)).isEmpty();
    }

    @Test
    @DisplayName("회원 수정은 닉네임을 검증하지만 가입 전용 아이디는 검증하지 않는다")
    void updateGroupChecksOnlyApplicableFields() {
        MemberVO vo = validUpdate();
        vo.setMember_nicknm(" ");
        vo.setLogin_id("");

        Set<ConstraintViolation<MemberVO>> violations = validator.validate(vo, UpdateGroup.class);

        assertThat(violations).anyMatch(v -> "member_nicknm".equals(v.getPropertyPath().toString()));
        assertThat(violations).noneMatch(v -> "login_id".equals(v.getPropertyPath().toString()));
    }

    private MemberVO validSignUp() {
        MemberVO vo = new MemberVO();
        vo.setLogin_id("testuser01");
        vo.setMember_pwd("password123");
        vo.setMember_email("test@example.com");
        vo.setMember_nicknm("테스터");
        vo.setMember_tel_no("010-1234-5678");
        return vo;
    }

    private MemberVO validUpdate() {
        MemberVO vo = new MemberVO();
        vo.setMember_nicknm("수정닉네임");
        vo.setMember_tel_no("010-9876-5432");
        return vo;
    }
}
