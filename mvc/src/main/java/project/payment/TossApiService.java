package project.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

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
            log.error("토스 API 에러: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());

            TossPaymentResponse errorResponse = readErrorResponse(e);
            // 5xx 응답은 승인 처리 여부를 단정할 수 없으므로 재조회 대상으로 남긴다.
            if (e.getStatusCode().is5xxServerError()) {
                errorResponse.setCode("CONFIRM_UNKNOWN");
            } else if (errorResponse.getCode() == null) {
                errorResponse.setCode("TOSS_API_ERROR");
            }
            if (errorResponse.getMessage() == null) {
                errorResponse.setMessage("결제 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
            }
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
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    .header("Idempotency-Key", "cancel-" + paymentKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("토스 결제 취소 완료: paymentKey={}, 사유={}", paymentKey, cancelReason);

        } catch (WebClientResponseException e) {
            log.error("토스 결제 취소 API 오류: paymentKey={}, status={}, body={}",
                    paymentKey, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("토스 결제 취소 실패 (API 오류): " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("토스 결제 취소 실패: paymentKey={}", paymentKey, e);
            throw new RuntimeException("토스 결제 취소 실패", e);
        }
    }

    /**
     * 승인 응답을 받지 못한 결제의 실제 상태를 조회한다.
     */
    public TossPaymentResponse getPayment(String paymentKey) {
        try {
            return tossPaymentWebClient
                    .get()
                    .uri("/v1/payments/{paymentKey}", paymentKey)
                    .retrieve()
                    .bodyToMono(TossPaymentResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.warn("토스 결제 상태 조회 응답 오류: paymentKey={}, status={}", paymentKey, e.getStatusCode());
            return readErrorResponse(e);
        } catch (Exception e) {
            log.error("토스 결제 상태 조회 실패: paymentKey={}", paymentKey, e);
            return null;
        }
    }

    private TossPaymentResponse readErrorResponse(WebClientResponseException e) {
        try {
            return objectMapper.readValue(e.getResponseBodyAsByteArray(), TossPaymentResponse.class);
        } catch (Exception parseException) {
            log.warn("토스 오류 응답 파싱 실패: status={}", e.getStatusCode(), parseException);
            return new TossPaymentResponse();
        }
    }
}
