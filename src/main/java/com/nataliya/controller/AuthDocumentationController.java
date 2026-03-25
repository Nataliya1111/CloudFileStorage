package com.nataliya.controller;

import com.nataliya.dto.error.ErrorResponseDto;
import com.nataliya.dto.user.UserAuthenticationRequestDto;
import com.nataliya.dto.user.UsernameResponseDto;
import com.nataliya.openapi.annotations.InternalServerErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration, login and logout")
@InternalServerErrorResponse
public class AuthDocumentationController {

    @Operation(
            summary = "User login",
            description = "Authenticates the user and creates a session"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = UsernameResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request. For example, username or password contains forbidden characters",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Incorrect username or password",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @PostMapping("/sign-in")
    public void login(@RequestBody UserAuthenticationRequestDto userRequestDto) {

    }

    @Operation(
            summary = "User logout",
            description = "Logs out the current user and invalidates the session"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Logout successful"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized. Authentication is required for this request"
            )
    })
    @PostMapping("/sign-out")
    public void signOut() {

    }
}
