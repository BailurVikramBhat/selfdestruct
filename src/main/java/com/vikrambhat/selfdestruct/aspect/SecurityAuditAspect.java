package com.vikrambhat.selfdestruct.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SecurityAuditAspect {
    private static final Logger logger = LoggerFactory.getLogger(SecurityAuditAspect.class);

    @Pointcut("execution(* com.vikrambhat.selfdestruct.service.SecretService.getSecret(..))")
    public void accessSecretMethods() {
    }

    @AfterReturning(pointcut = "accessSecretMethods()", returning = "result")
    public void logAccess(JoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        String secretId = (args.length > 0) ? args[0].toString() : "UNKNOWN";
        if(result!=null) {
            logger.info("AUDIT: [SECURITY ALERT]: Secret ID [{}] was accessed and destroyed. Content masked: *****", secretId);
        } else {
            logger.warn("AUDIT: [FAILED ATTEMPT] User tried to access expired Secret ID [{}]", secretId);
        }

    }
}
