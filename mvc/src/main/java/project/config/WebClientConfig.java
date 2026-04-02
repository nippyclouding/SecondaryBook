package project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.util.Base64;

@Configuration
public class WebClientConfig {

    // 외부 API 연동하는 클래스인데 아래는 예시 방식입니다 ! API 문서 잘 읽고 각자 파트 잘 넣어주세영 ~
    // resources - api.properties에 실제 발급한 키를 넣으면 됩니다 ! 해당 경로는 .gitignore에 올려서 외부 노출이 안되게 해야해영 ~
    

    // API 키들 주입
    @Value("${api.toss.secret-key}")
    private String tossSecretKey;

    @Value("${api.toss.base-url}")
    private String tossBaseUrl;

    // 카카오 설정 (application.properties에 있는지 확인 필요)
    @Value("${api.kakao.rest-api-key}")
    private String kakaoRestApiKey; // REST API 키 (Client ID)

    @Value("${api.kakao.login-url}") // https://kauth.kakao.com
    private String kakaoLoginUrl;

    @Value("${api.kakao.api-url}") // https://kapi.kakao.com (사용자 정보 조회용)
    private String kakaoApiUrl;

    // 네이버 설정
    @Value("${api.naver.client-id}")
    private String naverClientId;

    @Value("${api.naver.client-secret}")
    private String naverClientSecret;

    @Value("${api.naver.login-url}") // https://nid.naver.com
    private String naverLoginUrl;

    @Value("${api.naver.api-url}") // https://openapi.naver.com (사용자 정보 조회용)
    private String naverApiUrl;

    @Value("${api.kakao.book-url}")
    private String kakaoBookUrl;

    /*
    @Value("${api.kakao.rest-api-key}")
    private String kakaoRestApiKey;

    @Value("${api.kakao.book-url}")
    private String kakaoBookUrl;

    @Value("${api.kakao.login-url}")
    private String kakaoLoginUrl;

    @Value("${api.kakao.map-url}")
    private String kakaoMapUrl;

    @Value("${api.naver.client-id}")
    private String naverClientId;

    @Value("${api.naver.client-secret}")
    private String naverClientSecret;

    @Value("${api.naver.login-url}")
    private String naverLoginUrl;

     */

    // 1. 토스 결제 API
    @Bean
    public WebClient tossPaymentWebClient() {
        // 토스는 시크릿 키를 Base64로 인코딩해서 Authorization 헤더에 넣어야 함
        String encodedAuth = Base64.getEncoder()
                .encodeToString((tossSecretKey + ":").getBytes());

        // http 커넥션풀 설정
        ConnectionProvider provider = ConnectionProvider.builder("toss-payment")
                .maxConnections(50)
                .build();

        HttpClient httpClient = HttpClient.create(provider);

        return WebClient.builder()
                .baseUrl(tossBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    // 2. 카카오 로그인 (토큰 발급용)
    @Bean
    public WebClient kakaoAuthWebClient() {
        ConnectionProvider provider = ConnectionProvider.builder("kakao-auth")
                .maxConnections(50)
                .build();
        HttpClient httpClient = HttpClient.create(provider);

        return WebClient.builder()
                .baseUrl(kakaoLoginUrl) // https://kauth.kakao.com
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    // 3. 카카오 API (사용자 정보 조회용)
    @Bean
    public WebClient kakaoApiWebClient() {
        ConnectionProvider provider = ConnectionProvider.builder("kakao-api")
                .maxConnections(50)
                .build();
        HttpClient httpClient = HttpClient.create(provider);

        return WebClient.builder()
                .baseUrl(kakaoApiUrl) // https://kapi.kakao.com
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    // 4. 네이버 로그인 (토큰 발급용)
    @Bean
    public WebClient naverAuthWebClient() {
        ConnectionProvider provider = ConnectionProvider.builder("naver-auth")
                .maxConnections(50)
                .build();
        HttpClient httpClient = HttpClient.create(provider);

        return WebClient.builder()
                .baseUrl(naverLoginUrl) // https://nid.naver.com
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    // 5. 네이버 API (사용자 정보 조회용)
    @Bean
    public WebClient naverApiWebClient() {
        ConnectionProvider provider = ConnectionProvider.builder("naver-api")
                .maxConnections(50)
                .build();
        HttpClient httpClient = HttpClient.create(provider);

        return WebClient.builder()
                .baseUrl(naverApiUrl) // https://openapi.naver.com
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    // 6. 카카오 도서 검색 API
    @Bean
    public WebClient kakaoBookWebClient() {
        // http 커넥션풀 설정
        ConnectionProvider provider = ConnectionProvider.builder("kakao-book")
                .maxConnections(100)
                .build();

        HttpClient httpClient = HttpClient.create(provider);

        return WebClient.builder()
                .baseUrl(kakaoBookUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoRestApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }


    /*
    // 3. 카카오 로그인 API
    // http 커넥션풀 설정
    @Bean
    public WebClient kakaoLoginWebClient() {
        ConnectionProvider provider = ConnectionProvider.builder("kakao-login")
                .maxConnections(50)
                .build();

        HttpClient httpClient = HttpClient.create(provider);

        return WebClient.builder()
                .baseUrl(kakaoLoginUrl)
                // 로그인은 요청마다 다른 토큰 사용하므로 defaultHeader 안 씀
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    // 4. 네이버 로그인 API
    @Bean
    public WebClient naverLoginWebClient() {
        // http 커넥션풀 설정
        ConnectionProvider provider = ConnectionProvider.builder("naver-login")
                .maxConnections(50)
                .build();

        HttpClient httpClient = HttpClient.create(provider);

        
        return WebClient.builder()
                .baseUrl(naverLoginUrl)
                .defaultHeader("X-Naver-Client-Id", naverClientId)
                .defaultHeader("X-Naver-Client-Secret", naverClientSecret)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    // 5. 카카오(다음) 지도 API
    @Bean
    public WebClient kakaoMapWebClient() {
        ConnectionProvider provider = ConnectionProvider.builder("kakao-map")
                .maxConnections(100)
                .build();

        HttpClient httpClient = HttpClient.create(provider);

        return WebClient.builder()
                .baseUrl(kakaoMapUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoRestApiKey)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
    */
}