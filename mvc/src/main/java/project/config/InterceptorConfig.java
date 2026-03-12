package project.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import project.util.interceptor.AdminAuthInterceptor;
import project.util.interceptor.LoginRequiredInterceptor;
import project.util.interceptor.MemberActivityInterceptor;
import project.util.interceptor.UnreadInterceptor;

@Configuration
@RequiredArgsConstructor
public class InterceptorConfig implements WebMvcConfigurer {

    private final UnreadInterceptor unreadInterceptor;
    private final MemberActivityInterceptor memberActivityInterceptor;
    private final AdminAuthInterceptor adminAuthInterceptor;
    private final LoginRequiredInterceptor loginRequiredInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 1. 관리자 인증 인터셉터
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admin", "/admin/**")
                .excludePathPatterns(
                        "/admin/access",
                        "/admin/login",
                        "/admin/loginProcess",
                        "/admin/api/logout-pending",
                        "/admin/api/cancel-logout",
                        "/css/**", "/js/**", "/img/**"
                );

        // 2. 회원 로그인 필수 인터셉터
        registry.addInterceptor(loginRequiredInterceptor)
                .addPathPatterns(
                        // 거래 관련 (등록/수정/삭제/찜/판매완료)
                        "/trade",                  // POST: 판매글 등록
                        "/trade/modify/**",        // 수정 페이지 및 처리
                        "/trade/delete/**",        // 삭제
                        "/trade/like",             // 찜하기
                        "/trade/sold",             // 판매완료 처리
                        "/trade/confirm/**",       // 구매확정

                        // 결제 관련
                        "/payments",               // 결제 페이지
                        "/payments/**",            // 결제 처리

                        // 채팅 관련
                        "/chat/**",

                        // 마이페이지
                        "/mypage",
                        "/mypage/**",

                        // 주소 관련
                        "/profile/address/**",

                        // 계좌 관련
                        "/profile/bankaccount/**",

                        // 정산 관련
                        "/settlement/**",

                        // 회원 정보 수정
                        "/member/update",
                        "/member/delete",

                        // 북클럽 (상태 변경이 필요한 요청)
                        "/bookclubs/create",
                        "/bookclubs/**/join",
                        "/bookclubs/**/leave",
                        "/bookclubs/**/manage",
                        "/bookclubs/**/manage/**",
                        "/bookclubs/**/wish",
                        "/bookclubs/**/posts/write",
                        "/bookclubs/**/posts/**/edit",
                        "/bookclubs/**/posts/**/delete"
                )
                .excludePathPatterns(
                        "/css/**", "/js/**", "/img/**", "/resources/**"
                );

        // 3. 읽지 않은 메시지 체크 인터셉터
        registry.addInterceptor(unreadInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**");

        // 4. 회원 활동 추적 인터셉터
        registry.addInterceptor(memberActivityInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/admin/**",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/api/member/logout-pending",
                        "/api/member/cancel-logout"
                );
    }
}
