package com.nataliya.exception;

import org.springframework.security.core.AuthenticationException;

public class AuthConstraintViolationException extends AuthenticationException {

    public AuthConstraintViolationException(String message) {
        super(message);
    }

}
