package org.example.taskflowd.common.annotation;

import org.example.taskflowd.domain.activityLog.enums.ActLogEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivityLogger {

    ActLogEnum type();
    int paramIndex() default -1;
}