package com.Uqar.utils.Aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspect for logging service method exceptions
 * 
 * Note: This aspect complements the other logging aspects:
 * - RequestLoggingAspect: Handles HTTP request/response logging
 * - PerformanceMetricsAspect: Handles performance metrics
 * - AuditAspect: Handles audit event logging
 * 
 * This aspect focuses on exception logging for service methods
 * to provide detailed error context when exceptions occur.
 */
@Component
@Aspect
public class ServiceExceptionLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceExceptionLoggingAspect.class);

    /**
     * Logs exceptions thrown in service methods with full context
     * Controllers are handled by RequestLoggingAspect
     */
    @AfterThrowing(
            pointcut = "execution(* com.Uqar.product.service.*.*(..)) || " +
                      "execution(* com.Uqar.user.service.*.*(..)) || " +
                      "execution(* com.Uqar.purchase.service.*.*(..)) || " +
                      "execution(* com.Uqar.sale.service.*.*(..)) || " +
                      "execution(* com.Uqar.moneybox.service.*.*(..)) || " +
                      "execution(* com.Uqar.notification.service.*.*(..)) || " +
                      "execution(* com.Uqar.reports.service.*.*(..)) || " +
                      "execution(* com.Uqar.complaint.service.*.*(..))",
            throwing = "exception"
    )
    public void logServiceException(JoinPoint joinPoint, Throwable exception) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String exceptionType = exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();
        
        logger.error("Exception in service method: {}.{}() | Exception: {} | Message: {}", 
            className, methodName, exceptionType, exceptionMessage, exception);
    }
}

