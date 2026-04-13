package com.nataliya.controller;

import com.nataliya.controller.openapi.AuthOpenApi;
import com.nataliya.dto.request.user.UserRegistrationRequestDto;
import com.nataliya.dto.response.user.UsernameResponseDto;
import com.nataliya.security.service.LoginService;
import com.nataliya.service.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthOpenApi {

    private final RegistrationService registrationService;
    private final LoginService loginService;

    @Override
    @PostMapping("/sign-up")
    public ResponseEntity<UsernameResponseDto> register(
            UserRegistrationRequestDto userRequestDto,
            HttpServletRequest request,
            HttpServletResponse response) {

        UsernameResponseDto userResponseDto = registrationService.registerUser(userRequestDto);
        loginService.loginUser(userRequestDto, request, response);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(userResponseDto);
    }
}
