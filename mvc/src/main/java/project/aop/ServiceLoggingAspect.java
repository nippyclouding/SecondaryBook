package project.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect {

    // 서비스 계층 public 메서드에 aop 적용
    @Around("within(project..*Service)")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className  = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        StopWatch sw = new StopWatch();
        sw.start();

        try {
            Object result = joinPoint.proceed();
            sw.stop();
            log.info("[SERVICE] {}.{}() → {}ms", className, methodName, sw.getTotalTimeMillis());
            return result;

        } catch (Exception e) {
            sw.stop();
            log.error("[SERVICE] {}.{}() 예외 발생 ({}ms) — {}",
                    className, methodName, sw.getTotalTimeMillis(), e.getMessage());
            throw e;
        }
    }
}
