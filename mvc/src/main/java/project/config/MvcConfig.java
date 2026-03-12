package project.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.*;
import com.zaxxer.hikari.HikariDataSource;

import java.util.List;
import java.util.Properties;

@Configuration
@MapperScan(basePackages = { "project" }, annotationClass = Mapper.class)
@ComponentScan(basePackages = { "project" })
@EnableWebMvc
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
public class MvcConfig implements WebMvcConfigurer {

    @Value("${db.driver}")
    private String driver;
    @Value("${db.url}")
    private String url;
    @Value("${db.username}")
    private String username;
    @Value("${db.password}")
    private String password;

    // ================= Mail =================
    @Value("${mail.username}")
    private String mailUsername;
    @Value("${mail.password}")
    private String mailPassword;

    @Value("${file.dir}")
    private String uploadPath;

    // Jackson: LocalDateTime을 ISO 문자열로 직렬화
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        converters.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
        converters.add(0, new MappingJackson2HttpMessageConverter(om));
    }

    // JSP ViewResolver
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.jsp("/WEB-INF/views/", ".jsp");
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    // 정적 리소스 핸들러
    // servlet-context.xml의 <mvc:resources> 제거 후 이곳에서 일원화하여 관리
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 정적 리소스 (CSS, JS, 이미지 등)
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/resources/");
        registry.addResourceHandler("/css/**")
                .addResourceLocations("/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("/js/");

        // 로컬 업로드 이미지 서빙: /upload/** → file.dir 경로 매핑 (로컬 개발 환경용)
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }



    // hikaricp
    @Bean
    @Primary
    public HikariDataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driver);
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        // 공용 RDS를 위한 커넥션 풀 설정
        dataSource.setMaximumPoolSize(5); // 최대 커넥션 수 (공용이므로 작게)
        dataSource.setMinimumIdle(2); // 최소 유휴 커넥션
        dataSource.setConnectionTimeout(30000); // 커넥션 획득 대기 시간 (30초)
        dataSource.setIdleTimeout(600000); // 유휴 커넥션 유지 시간 (10분)
        dataSource.setMaxLifetime(1800000); // 커넥션 최대 수명 (30분)

        return dataSource;
    }

    // mybatis
    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean ssf = new SqlSessionFactoryBean();
        ssf.setDataSource(dataSource());

        // Java Config로 모든 MyBatis 설정
        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();

        config.setMapUnderscoreToCamelCase(false);
        config.setJdbcTypeForNull(org.apache.ibatis.type.JdbcType.NULL);
        config.setLogImpl(org.apache.ibatis.logging.slf4j.Slf4jImpl.class);

        ssf.setConfiguration(config);

        org.springframework.core.io.support.PathMatchingResourcePatternResolver resolver = new org.springframework.core.io.support.PathMatchingResourcePatternResolver();
        ssf.setMapperLocations(resolver.getResources("classpath:**/*Mapper.xml"));

        return ssf.getObject();
    }

    // ================= TransactionManager =================
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean(name = "filterMultipartResolver")
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername(mailUsername);
        mailSender.setPassword(mailPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        return mailSender;
    }

}
