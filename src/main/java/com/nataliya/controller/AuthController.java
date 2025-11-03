package com.nataliya.controller;

import com.nataliya.dto.UserRegistrationRequestDto;
import com.nataliya.dto.UserRegistrationResponseDto;
import com.nataliya.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<UserRegistrationResponseDto> register(
            @Valid @RequestBody UserRegistrationRequestDto userRequestDto
    ) {

        UserRegistrationResponseDto userResponseDto = userService.register(userRequestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(userResponseDto);
    }


}
