package com.nexxmc.database;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {METHOD})
public @interface Query {
    enum Type {SELECT, INSERT, UPDATE, DELETE}

    String[] select() default "";
    String[] set() default "";
    String[] insert() default "";
    String[] from() default "";
    String into() default "";
    String[] where() default "";

    Type type();
}
