package project.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TossApiService {

    private final WebClient tossPaymentWebClient;  // WebClientConfig에서 주입

    /**
     * 토스 결제 승인 API 호출
     */
    public TossPaymentResponse confirmPayment(String paymentKey, String orderId, int amount) {

        // 1. 요청 바디 생성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentKey", paymentKey);
        requestBody.put("orderId", orderId);
        requestBody.put("amount", amount);

        try {
            // 2. WebClient로 POST 요청 (orderId를 멱등키로 사용하여 이중 결제 방지)
            TossPaymentResponse response = tossPaymentWebClient
                    .post()
                    .uri("/v1/payments/confirm")
                    .header("Idempotency-Key", orderId)
                    .bodyValue(requestBody)           // JSON 바디
                    .retrieve()                       // 응답 받기
                    .bodyToMono(TossPaymentResponse.class)  // JSON → 객체 변환
                    .block();                         // 동기 처리 (결과 기다림)

            log.info("토스 결제 승인 성공: {}", response);
            return response;

        } catch (WebClientResponseException e) {
            // 3. 토스 API 에러 응답 처리
            log.error("토스 API 에러: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());

            TossPaymentResponse errorResponse = new TossPaymentResponse();
            errorResponse.setCode("TOSS_API_ERROR");
            errorResponse.setMessage("결제 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
            return errorResponse;

        } catch (Exception e) {
            log.error("토스 결제 승인 실패", e);

            TossPaymentResponse errorResponse = new TossPaymentResponse();
            errorResponse.setCode("UNKNOWN_ERROR");
            errorResponse.setMessage("결제 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
            return errorResponse;
        }
    }

    /**
     * 토스 결제 취소 API 호출 (DB 처리 실패 시 자동 환불)
     *
     * @param paymentKey  취소할 결제의 paymentKey
     * @param cancelReason 취소 사유 (토스 측 기록용)
     */
    public void cancelPayment(String paymentKey, String cancelReason) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("cancelReason", cancelReason);

        try {
            tossPaymentWebClient
                    .post()
                    .uri("/v1/payments/" + paymentKey + "/cancel")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("토스 결제 취소 완료: paymentKey={}, 사유={}", paymentKey, cancelReason);

        } catch (WebClientResponseException e) {
            log.error("토스 결제 취소 API 오류: paymentKey={}, status={}, body={}",
                    paymentKey, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("토스 결제 취소 실패: paymentKey={}", paymentKey, e);
        }
    }
}
