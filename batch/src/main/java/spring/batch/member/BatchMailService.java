package spring.batch.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@Slf4j
@RequiredArgsConstructor
public class BatchMailService {

    private final JavaMailSender mailSender;

    @Value("${mail.username}")
    private String fromEmail;

    public void sendInsufficientBalanceEmail(String toEmail,
                                             String sellerNicknm,
                                             int settlementAmount,
                                             long tradeSeq) {
        String subject = "[SecondHand Books] 정산 처리가 일시 지연되었습니다";
        String content = buildEmailContent(sellerNicknm, settlementAmount, tradeSeq);
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

    private String buildEmailContent(String sellerNicknm, int settlementAmount, long tradeSeq) {
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
}
