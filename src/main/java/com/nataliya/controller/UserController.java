package com.nataliya.controller;

import com.nataliya.dto.error.ErrorResponseDto;
import com.nataliya.dto.user.UsernameResponseDto;
import com.nataliya.openapi.annotations.InternalServerErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Tag(name = "Users", description = "Operations related to the current user")
@InternalServerErrorResponse
public class UserController {

    @Operation(
            summary = "Get current user information",
            description = "Returns information about the currently authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User information retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized. Authentication is required for this request",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @GetMapping("/me")
    public UsernameResponseDto getUser(@AuthenticationPrincipal UserDetails userDetails) {
        return new UsernameResponseDto(userDetails.getUsername());
    }
}
