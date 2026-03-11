package com.nataliya.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MovePathValidator.class)
public @interface ValidMovePath {

    String message() default "Invalid move operation";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
