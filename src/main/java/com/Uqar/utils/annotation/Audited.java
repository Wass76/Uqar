package com.Uqar.utils.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for audit logging
 * When applied, the AuditAspect will record audit events for the method
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    /**
     * The action being performed (e.g., "CREATE_USER", "UPDATE_PRODUCT")
     */
    String action();
    
    /**
     * The type of target entity (e.g., "USER", "PRODUCT")
     * If empty, will be inferred from the class name
     */
    String targetType() default "";
    
    /**
     * Whether to include method arguments in audit details
     */
    boolean includeArgs() default false;
}

