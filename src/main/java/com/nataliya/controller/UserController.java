package com.nataliya.controller;

import com.nataliya.dto.user.UsernameResponseDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/me")
    public UsernameResponseDto getUser(@AuthenticationPrincipal UserDetails userDetails) {
        return new UsernameResponseDto(userDetails.getUsername());
    }
}
