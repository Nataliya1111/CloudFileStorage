package com.nataliya.controller;

import com.nataliya.controller.openapi.UserOpenApi;
import com.nataliya.dto.response.user.UsernameResponseDto;
import com.nataliya.security.model.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController implements UserOpenApi {

    @Override
    @GetMapping("/me")
    public UsernameResponseDto getUser(@AuthenticationPrincipal AuthenticatedUser userDetails) {
        return new UsernameResponseDto(userDetails.getUsername());
    }
}
