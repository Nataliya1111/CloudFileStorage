package com.nataliya.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nataliya.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JsonAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException e)
            throws IOException {

        String errorMessage;

        if (e instanceof BadCredentialsException && e.getCause() instanceof ConstraintViolationException) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());  //400
            errorMessage = e.getMessage();
        } else if (e instanceof BadCredentialsException) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());  //401
            errorMessage = "Invalid username or password";
        } else {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorMessage = e.getMessage();
        }

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(errorMessage);
        objectMapper.writeValue(response.getWriter(), errorResponseDto);
    }

}
