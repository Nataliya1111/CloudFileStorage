package com.nataliya.controller;

import com.nataliya.dto.UserRegistrationRequestDto;
import com.nataliya.dto.UsernameResponseDto;
import com.nataliya.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<UsernameResponseDto> register(
            @Valid @RequestBody UserRegistrationRequestDto userRequestDto
    ) {

        UsernameResponseDto userResponseDto = userService.register(userRequestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(userResponseDto);
    }

    @GetMapping("/me")
    public Authentication me(Authentication authentication) {
        return authentication;
    }

}
