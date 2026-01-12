package com.nataliya.controller;

import com.nataliya.dto.UserRegistrationRequestDto;
import com.nataliya.dto.UsernameResponseDto;
import com.nataliya.service.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegistrationService registrationService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    @PostMapping("/sign-up")
    public ResponseEntity<UsernameResponseDto> register(
            @Valid @RequestBody UserRegistrationRequestDto userRequestDto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        UsernameResponseDto userResponseDto = registrationService.registerUser(userRequestDto);

        UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken
                .unauthenticated(
                        userRequestDto.username(),
                        userRequestDto.password()
                );

        Authentication auth = authenticationManager.authenticate(authRequest);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);

        securityContextRepository.saveContext(securityContext, request, response);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(userResponseDto);
    }

}
