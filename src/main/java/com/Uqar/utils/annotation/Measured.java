package com.Uqar.utils.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for performance metrics collection
 * When applied, the PerformanceMetricsAspect will record execution time metrics
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Measured {
    /**
     * Custom metric name (optional)
     * If not provided, will be generated from class and method name
     */
    String name() default "";
}

