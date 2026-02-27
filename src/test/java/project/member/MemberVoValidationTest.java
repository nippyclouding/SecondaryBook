package project.member;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import project.member.validation.SignUpGroup;
import project.member.validation.UpdateGroup;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MemberVO Bean Validation 테스트
 * - 실제 Hibernate Validator 구현체로 검증
 * - SignUpGroup / UpdateGroup 별 제약조건 검증
 */
@DisplayName("MemberVO - Bean Validation")
class MemberVoValidationTest {

    static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory();
        validator = factory.getValidator();
    }

    // ========== 헬퍼 ==========

    private MemberVO validSignUpVO() {
        MemberVO vo = new MemberVO();
        vo.setLogin_id("testuser01");
        vo.setMember_pwd("password123");
        vo.setMember_email("test@example.com");
        vo.setMember_nicknm("테스터");
        vo.setMember_tel_no("010-1234-5678");
        return vo;
    }

    private MemberVO validUpdateVO() {
        MemberVO vo = new MemberVO();
        vo.setMember_nicknm("수정닉네임");
        vo.setMember_tel_no("010-9876-5432");
        return vo;
    }

    // ========================================================================
    // SignUpGroup 검증
    // ========================================================================
    @Nested
    @DisplayName("SignUpGroup - 회원가입 필드 검증")
    class SignUpValidation {

        @Test
        @DisplayName("모든 필드가 유효하면 위반이 없다")
        void allValid_noViolations() {
            Set<ConstraintViolation<MemberVO>> violations =
                    validator.validate(validSignUpVO(), SignUpGroup.class);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("login_id가 비어있으면 위반 발생")
        void loginId_blank_fails() {
            MemberVO vo = validSignUpVO();
            vo.setLogin_id("");
            Set<ConstraintViolation<MemberVO>> v = validator.validate(vo, SignUpGroup.class);
            assertThat(v).anyMatch(c -> c.getPropertyPath().toString().equals("login_id"));
        }

        @Test
        @DisplayName("login_id가 3자이면 위반 발생 (최소 4자)")
        void loginId_tooShort_fails() {
            MemberVO vo = validSignUpVO();
            vo.setLogin_id("abc");
            Set<ConstraintViolation<MemberVO>> v = validator.validate(vo, SignUpGroup.class);
            assertThat(v).anyMatch(c -> c.getPropertyPath().toString().equals("login_id"));
        }

        @Test
        @DisplayName("login_id가 21자이면 위반 발생 (최대 20자)")
        void loginId_tooLong_fails() {
            MemberVO vo = validSignUpVO();
            vo.setLogin_id("a".repeat(21));
            Set<ConstraintViolation<MemberVO>> v = validator.validate(vo, SignUpGroup.class);
            assertThat(v).anyMatch(c -> c.getPropertyPath().toString().equals("login_id"));
        }

        @Test
        @DisplayName("member_pwd가 7자이면 위반 발생 (최소 8자)")
        void password_tooShort_fails() {
            MemberVO vo = validSignUpVO();
            vo.setMember_pwd("1234567");
            Set<ConstraintViolation<MemberVO>> v = validator.validate(vo, SignUpGroup.class);
            assertThat(v).anyMatch(c -> c.getPropertyPath().toString().equals("member_pwd"));
        }

        @Test
        @DisplayName("member_pwd가 21자이면 위반 발생 (최대 20자)")
        void password_tooLong_fails() {
            MemberVO vo = validSignUpVO();
            vo.setMember_pwd("a".repeat(21));
            Set<ConstraintViolation<MemberVO>> v = validator.validate(vo, SignUpGroup.class);
            assertThat(v).anyMatch(c -> c.getPropertyPath().toString().equals("member_pwd"));
        }

        @Test
        @DisplayName("이메일 형식이 잘못되면 위반 발생")
        void email_invalidFormat_fails() {
            MemberVO vo = validSignUpVO();
            vo.setMember_email("not-an-email");
            Set<ConstraintViolation<MemberVO>> v = validator.validate(vo, SignUpGroup.class);
            assertThat(v).anyMatch(c -> c.getPropertyPath().toString().equals("member_email"));
        }

        @Test
        @DisplayName("닉네임이 비어있으면 위반 발생")
        void nickname_blank_fails() {
            MemberVO vo = validSignUpVO();
            vo.setMember_nicknm("");
            Set<ConstraintViolation<MemberVO>> v = validator.validate(vo, SignUpGroup.class);
            assertThat(v).anyMatch(c -> c.getPropertyPath().toString().equals("member_nicknm"));
        }

        @Test
        @DisplayName("닉네임이 1자이면 위반 발생 (최소 2자)")
        void nickname_tooShort_fails() {
            MemberVO vo = validSignUpVO();
            vo.setMember_nicknm("아");
            Set<ConstraintViolation<MemberVO>> v = validator.validate(vo, SignUpGroup.class);
            assertThat(v).anyMatch(c -> c.getPropertyPath().toString().equals("member_nicknm"));
        }

        @Test
        @DisplayName("전화번호가 9자리이면 위반 발생 (최소 10자리)")
        void telNo_tooShort_fails() {
            MemberVO vo = validSignUpVO();
            vo.setMember_tel_no("010123456");
            Set<ConstraintViolation<MemberVO>> v = validator.validate(vo, SignUpGroup.class);
            assertThat(v).anyMatch(c -> c.getPropertyPath().toString().equals("member_tel_no"));
        }

        @Test
        @DisplayName("전화번호가 숫자가 아니면 위반 발생")
        void telNo_nonDigit_fails() {
            MemberVO vo = validSignUpVO();
            vo.setMember_tel_no("0101234ABCD");
            Set<ConstraintViolation<MemberVO>> v = validator.validate(vo, SignUpGroup.class);
            assertThat(v).anyMatch(c -> c.getPropertyPath().toString().equals("member_tel_no"));
        }

        @Test
        @DisplayName("전화번호가 null이면 위반 없음 (선택 항목)")
        void telNo_null_ok() {
            MemberVO vo = validSignUpVO();
            vo.setMember_tel_no(null);
            Set<ConstraintViolation<MemberVO>> v = validator.validate(vo, SignUpGroup.class);
            assertThat(v).noneMatch(c -> c.getPropertyPath().toString().equals("member_tel_no"));
        }
    }

    // ========================================================================
    // UpdateGroup 검증
    // ========================================================================
    @Nested
    @DisplayName("UpdateGroup - 회원정보 수정 필드 검증")
    class UpdateValidation {

        @Test
        @DisplayName("유효한 수정 VO - 위반 없음")
        void validUpdate_noViolations() {
            Set<ConstraintViolation<MemberVO>> violations =
                    validator.validate(validUpdateVO(), UpdateGroup.class);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("닉네임 공백 - 위반 발생")
        void nickname_blank_fails() {
            MemberVO vo = validUpdateVO();
            vo.setMember_nicknm("   ");
            Set<ConstraintViolation<MemberVO>> v = validator.validate(vo, UpdateGroup.class);
            assertThat(v).anyMatch(c -> c.getPropertyPath().toString().equals("member_nicknm"));
        }

        @Test
        @DisplayName("SignUpGroup 전용 필드(login_id)는 UpdateGroup에서 검증 안 됨")
        void signUpOnly_field_not_validated_in_update() {
            MemberVO vo = validUpdateVO();
            vo.setLogin_id(""); // @NotBlank(groups=SignUpGroup) 이므로 UpdateGroup에서는 검증 안 됨
            Set<ConstraintViolation<MemberVO>> v = validator.validate(vo, UpdateGroup.class);
            assertThat(v).noneMatch(c -> c.getPropertyPath().toString().equals("login_id"));
        }
    }
}
