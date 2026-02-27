package project.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    @Value("${mail.username}")
    private String fromEmail;

    // [기존] 회원가입용 인증 이메일 발송
    public boolean sendAuthEmail(String email) {
        return sendEmailForm(email, "[SecondHand Books] 회원가입 인증번호 안내", "회원가입");
    }

    // [추가] 비밀번호 재설정용 인증 이메일 발송 (새로운 폼)
    public boolean sendPwdResetEmail(String email) {
        return sendEmailForm(email, "[SecondHand Books] 비밀번호 재설정 인증번호 안내", "비밀번호 재설정");
    }

    // [공통] 내부 이메일 발송 로직 (내용만 동적으로 변경)
    private boolean sendEmailForm(String email, String subject, String actionType) {
        // 1. 난수 생성
        Random random = new Random();
        String checkNum = String.valueOf(random.nextInt(888888) + 111111);

        // 2. Redis 저장 (유효기간 3분)
        try {
            redisTemplate.opsForValue().set("AuthCode:" + email, checkNum, 180, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis 저장 실패", e);
            return false;
        }

        // 3. 이메일 본문 작성
        StringBuilder content = new StringBuilder();
        content.append("<div style='font-family: \"Malgun Gothic\", sans-serif; background-color: #f4f5f7; padding: 40px 0;'>");
        content.append("  <div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; border: 1px solid #e1e4e8;'>");

        // 헤더 색상 변경 (비밀번호 찾기는 조금 더 경고 느낌이 나는 짙은 네이비 혹은 유지)
        content.append("    <div style='background-color: #1e3a8a; padding: 24px 30px;'>"); // 색상 변경 (Primary 900)
        content.append("      <h1 style='color: #ffffff; font-size: 20px; margin: 0;'>SecondHand Books</h1>");
        content.append("    </div>");

        content.append("    <div style='padding: 40px 30px;'>");
        content.append("      <h2 style='color: #1a1a1a; font-size: 24px; margin: 0 0 20px 0;'>" + actionType + " 인증번호</h2>");
        content.append("      <p style='color: #555555; font-size: 15px; line-height: 1.6;'>");
        content.append("        안녕하세요, 고객님.<br>");
        content.append("        요청하신 <strong>" + actionType + "</strong>을(를) 위해 아래 인증번호를 입력해주세요.");
        content.append("      </p>");

        content.append("      <div style='background-color: #F0F4FF; border-radius: 4px; padding: 30px; text-align: center; margin: 30px 0; border: 1px solid #D6E4FF;'>");
        content.append("        <span style='color: #0046FF; font-size: 32px; font-weight: 800; letter-spacing: 5px;'>" + checkNum + "</span>");
        content.append("      </div>");

        content.append("      <p style='color: #888; font-size: 13px;'>인증번호 유효 시간은 3분입니다.</p>");
        content.append("    </div>");
        content.append("  </div>");
        content.append("</div>");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(content.toString(), true);

            mailSender.send(message);
            return true;
        } catch (Exception e) {
            log.error("이메일 발송 실패", e);
            return false;
        }
    }

    // 잔액 부족으로 정산 지연 시 판매자 알림 이메일 발송
    public void sendInsufficientBalanceEmail(String toEmail,
                                             String sellerNicknm,
                                             int settlementAmount,
                                             long tradeSeq) {
        String subject = "[SecondHand Books] 정산 처리가 일시 지연되었습니다";
        String content = buildInsufficientBalanceEmailContent(sellerNicknm, settlementAmount, tradeSeq);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
            log.info("잔액부족 알림 이메일 발송 완료: to={}, tradeSeq={}", toEmail, tradeSeq);
        } catch (Exception e) {
            log.error("잔액부족 알림 이메일 발송 실패: to={}, tradeSeq={}", toEmail, tradeSeq, e);
        }
    }

    private String buildInsufficientBalanceEmailContent(String sellerNicknm,
                                                        int settlementAmount,
                                                        long tradeSeq) {
        return "<div style='font-family:\"Malgun Gothic\",sans-serif;background-color:#f4f5f7;padding:40px 0;'>"
                + "<div style='max-width:600px;margin:0 auto;background-color:#ffffff;border-radius:8px;border:1px solid #e1e4e8;'>"
                + "<div style='background-color:#1e3a8a;padding:24px 30px;'>"
                + "<h1 style='color:#ffffff;font-size:20px;margin:0;'>SecondHand Books</h1>"
                + "</div>"
                + "<div style='padding:40px 30px;'>"
                + "<h2 style='color:#1a1a1a;font-size:24px;margin:0 0 20px 0;'>정산 처리 일시 지연 안내</h2>"
                + "<p style='color:#555555;font-size:15px;line-height:1.6;'>"
                + sellerNicknm + " 님, 안녕하세요.<br><br>"
                + "거래 <strong>#" + tradeSeq + "</strong> 에 대해 신청하신 정산이<br>"
                + "현재 플랫폼 운영 계좌의 잔액 부족으로 <strong>일시적으로 지연</strong>되고 있습니다."
                + "</p>"
                + "<div style='background-color:#FFF7ED;border-radius:4px;padding:20px 24px;margin:24px 0;border:1px solid #FED7AA;'>"
                + "<p style='margin:0;color:#92400E;font-size:14px;'>"
                + "정산 예정 금액: <strong>" + String.format("%,d", settlementAmount) + "원</strong><br>"
                + "상태: 잔액 부족 (INSUFFICIENT_BALANCE)"
                + "</p>"
                + "</div>"
                + "<p style='color:#555555;font-size:15px;line-height:1.6;'>"
                + "관리자가 잔액을 충전한 후 자동으로 재처리됩니다.<br>"
                + "불편을 드려 죄송합니다."
                + "</p>"
                + "</div>"
                + "</div>"
                + "</div>";
    }

    // 인증번호 검증 (기존 유지)
    public boolean verifyEmailCode(String email, String code) {
        String savedCode = redisTemplate.opsForValue().get("AuthCode:" + email);
        if (savedCode != null && savedCode.equals(code)) {
            redisTemplate.delete("AuthCode:" + email);
            return true;
        }
        return false;
    }
}