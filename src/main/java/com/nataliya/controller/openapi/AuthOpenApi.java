package com.nataliya.controller.openapi;

import com.nataliya.dto.response.error.ErrorResponseDto;
import com.nataliya.dto.request.user.UserRegistrationRequestDto;
import com.nataliya.dto.response.user.UsernameResponseDto;
import com.nataliya.openapi.annotations.InternalServerErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Authentication", description = "User registration, login and logout")
@InternalServerErrorResponse
public interface AuthOpenApi {

    @Operation(
            summary = "Register a user",
            description = "Registers a new user, creates a root directory, and automatically logs the user in"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request. For example, username or password contains forbidden characters",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User with the same username already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    ResponseEntity<UsernameResponseDto> register(
            @Valid @RequestBody UserRegistrationRequestDto userRequestDto,
            HttpServletRequest request,
            HttpServletResponse response);
}
