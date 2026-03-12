package project.util.book;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class BookApiService {

    @Value("${api.kakao.book-url}")
    private String kakaoBookUrl;

    // AppConfig 에서 등록한 WebClient 주입
    private final WebClient kakaoBookWebClient;
    private final ObjectMapper objectMapper;

    public List<BookVO> searchBooks(String query) {
        List<BookVO> books = new ArrayList<>();

        try {
            String uri = UriComponentsBuilder.fromHttpUrl(kakaoBookUrl)
                    .queryParam("query", query)
                    .queryParam("size", 10)
                    .build()
                    .toUriString();

            log.info("Kakao API 호출: {}", uri);

            // WebClient 호출 (동기 처리)
            String responseBody = kakaoBookWebClient.get()
                    .uri(uri)
                    .retrieve()
                    .onStatus(
                            HttpStatus::isError,
                            response -> response.bodyToMono(String.class)
                                    .map(body -> {
                                        log.error("Kakao API 오류 응답: status={}, body={}",
                                                response.statusCode(), body);
                                        return new RuntimeException("Kakao API 호출 실패");
                                    })
                    )
                    .bodyToMono(String.class)
                    .block();

            if (responseBody == null) {
                log.warn("Kakao API 응답이 비어있음");
                return books;
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode documents = root.get("documents");

            if (documents != null && documents.isArray()) {
                for (JsonNode doc : documents) {
                    BookVO book = parseBookFromJson(doc);
                    books.add(book);
                }
            }

            log.info("검색 결과: {}건", books.size());

        } catch (WebClientResponseException e) {
            // HTTP 에러 (4xx, 5xx)
            log.error("Kakao API HTTP 오류: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

        } catch (Exception e) {
            // 기타 오류
            log.error("Kakao API 호출 실패", e);
        }

        return books;
    }


    // json -> book 데이터
    private BookVO parseBookFromJson(JsonNode jsonNode) {
        /* 응답 document 중 받아와야 하는 데이터
            title : String
            isbn : String
            authors : String[]
            publisher : String
            price : Integer
            thumbnail : String
         */


        // isbn (공백으로 구분된 경우 첫 번째 값 사용)
        String isbn = "";
        if (jsonNode.has("isbn")) {
            String isbnText = jsonNode.get("isbn").asText();
            if (!isbnText.isEmpty()) {
                isbn = isbnText.split(" ")[0];  // "9788972756194 1234567890" -> "9788972756194"
            }
        }

        // title
        String title = jsonNode.has("title") ? jsonNode.get("title").asText() : "";

        // author (배열로 올 경우 첫 번째 값 사용)
        String author = "";
        if (jsonNode.has("authors") && jsonNode.get("authors").isArray() && jsonNode.get("authors").size() > 0) {
            author = jsonNode.get("authors").get(0).asText();
        }

        // publisher
        String publisher = jsonNode.has("publisher") ? jsonNode.get("publisher").asText() : "";

        // thumbnail
        String thumbnail = jsonNode.has("thumbnail") ? jsonNode.get("thumbnail").asText() : "";

        // org_price
        int price = jsonNode.has("price") ? jsonNode.get("price").asInt() : 0;

        return new BookVO(isbn, title, author, publisher, thumbnail, price);
    }
}
