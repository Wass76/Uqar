package com.Uqar.utils.Aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.Uqar.utils.annotation.Measured;

import java.lang.reflect.Method;

/**
 * Aspect for recording performance metrics
 * Records execution time for service methods
 * 
 * Note: This aspect logs execution times. If Micrometer is needed in the future,
 * it can be added as an optional dependency with proper MeterRegistry injection.
 */
@Aspect
@Component
public class PerformanceMetricsAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMetricsAspect.class);

    @Around("execution(* com.Uqar.product.service.*.*(..)) || " +
            "execution(* com.Uqar.user.service.*.*(..)) || " +
            "execution(* com.Uqar.purchase.service.*.*(..)) || " +
            "execution(* com.Uqar.sale.service.*.*(..)) || " +
            "execution(* com.Uqar.moneybox.service.*.*(..)) || " +
            "execution(* com.Uqar.notification.service.*.*(..)) || " +
            "execution(* com.Uqar.reports.service.*.*(..)) || " +
            "execution(* com.Uqar.complaint.service.*.*(..)) || " +
            "@annotation(com.Uqar.utils.annotation.Measured)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        // Always log execution time, and optionally record metrics if Micrometer is available
        return logExecutionTime(joinPoint);
    }
    
    private Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String metricName = "uqar.service." + className.toLowerCase() + "." + methodName + ".duration";
        
        // Check for custom metric name from @Measured annotation
        Measured measured = null;
        if (joinPoint.getSignature() instanceof MethodSignature methodSignature) {
            Method method = methodSignature.getMethod();
            measured = method.getAnnotation(Measured.class);
            if (measured == null) {
                measured = method.getDeclaringClass().getAnnotation(Measured.class);
            }
        }
        if (measured != null && !measured.name().isEmpty()) {
            metricName = "uqar.service." + measured.name() + ".duration";
        }
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // Log execution time
            logger.debug("Execution time for {}.{}: {}ms", className, methodName, duration);
            
            // Try to record metric using Micrometer if available
            recordMetricIfAvailable(metricName, duration, className, methodName);
            
            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Execution time for {}.{} (failed): {}ms", className, methodName, duration);
            throw e;
        }
    }
    
    private void recordMetricIfAvailable(String metricName, long duration, String className, String methodName) {
        // Log metric information - can be extended to use Micrometer if needed
        // For now, execution time is logged via logger.debug above
        logger.trace("Metric {}: {}ms for {}.{}", metricName, duration, className, methodName);
    }
}

