package com.Uqar.utils.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods or classes for request logging
 * When applied, the RequestLoggingAspect will log HTTP request details
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Loggable {
    /**
     * Whether to log request payload (default: false for security)
     */
    boolean logPayload() default false;
    
    /**
     * Whether to log response payload (default: false for security)
     */
    boolean logResponse() default false;
}

