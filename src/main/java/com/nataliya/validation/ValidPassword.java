package com.nataliya.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@NotBlank(message = "Password must not be empty")
@Size(
        min = 5, max = 20,
        message = "Password must be {min} to {max} characters long"
)
@Pattern(
        regexp = "^[a-zA-Z0-9!@#$%^&*(),.?\":{}|<>\\\\/`~+=-_';]*$",
        message = "Password contains invalid characters. Only Latin letters, digits and common symbols are allowed"
)
public @interface ValidPassword {

    String message() default "Invalid password";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
