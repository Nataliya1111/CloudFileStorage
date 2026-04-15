package com.nataliya.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nataliya.dto.request.user.UserAuthenticationRequestDto;
import com.nataliya.exception.AuthConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;

import jakarta.validation.Validator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextRepository;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final String SIGN_IN_URL = "/api/auth/sign-in";

    private final ObjectMapper objectMapper;
    private final Validator validator1;

    public JsonUsernamePasswordAuthenticationFilter(
            AuthenticationManager authenticationManager,
            Validator validator,
            ObjectMapper objectMapper,
            AuthenticationSuccessHandler successHandler,
            AuthenticationFailureHandler failureHandler,
            SecurityContextRepository securityContextRepository

    ) {
        super(authenticationManager);
        setFilterProcessesUrl(SIGN_IN_URL);
        setAuthenticationSuccessHandler(successHandler);
        setAuthenticationFailureHandler(failureHandler);
        setSecurityContextRepository(securityContextRepository);
        this.validator1 = validator;
        this.objectMapper = objectMapper;

    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {

        if (!"POST".equals(request.getMethod())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        } else {

            try {
                UserAuthenticationRequestDto userRequestDto = objectMapper.readValue(request.getInputStream(), UserAuthenticationRequestDto.class);

                Set<ConstraintViolation<UserAuthenticationRequestDto>> violations =
                        validator1.validate(userRequestDto);

                if (!violations.isEmpty()) {
                    String message = violations.stream()
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.joining("; "));

                    throw new AuthConstraintViolationException(message);
                }

                String username = userRequestDto.username();
                String password = userRequestDto.password();
                UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(username, password);
                this.setDetails(request, authRequest);
                return this.getAuthenticationManager().authenticate(authRequest);

            } catch (IOException e) {
                throw new AuthenticationServiceException("Invalid request body", e);
            }
        }
    }

}
