package com.qanunqapisi.config.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    @Pointcut("within(com.qanunqapisi.service..*)")
    public void serviceLayer() {
    }

    @Pointcut("within(com.qanunqapisi.controller..*)")
    public void controllerLayer() {
    }

    @Around("serviceLayer() && execution(* com.qanunqapisi.service.AdminUserService.*(..))")
    public Object logAdminUserOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        log.info("Admin operation: {} - Args: {}", methodName, joinPoint.getArgs());

        try {
            Object result = joinPoint.proceed();
            log.info("Admin operation completed: {}", methodName);
            return result;
        } catch (Exception e) {
            log.error("Admin operation failed: {} - Error: {}", methodName, e.getMessage());
            throw e;
        }
    }

    @Around("serviceLayer() && execution(* com.qanunqapisi.service.TestService.deleteTest(..))")
    public Object logTestDeletion(ProceedingJoinPoint joinPoint) throws Throwable {
        log.warn("Test deletion requested - Test ID: {}", joinPoint.getArgs()[0]);
        Object result = joinPoint.proceed();
        log.warn("Test deleted - Test ID: {}", joinPoint.getArgs()[0]);
        return result;
    }

    @AfterThrowing(pointcut = "serviceLayer() || controllerLayer()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        log.error("Exception in {}.{}() with cause = {}",
            joinPoint.getSignature().getDeclaringTypeName(),
            joinPoint.getSignature().getName(),
            ex.getCause() != null ? ex.getCause() : "NULL");
    }
}
