package com.nataliya.controller.openapi;

import com.nataliya.dto.response.error.ErrorResponseDto;
import com.nataliya.dto.response.user.UsernameResponseDto;
import com.nataliya.openapi.annotations.InternalServerErrorResponse;
import com.nataliya.security.model.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Users", description = "Operations related to the current user")
@InternalServerErrorResponse
public interface UserOpenApi {

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
    UsernameResponseDto getUser(AuthenticatedUser userDetails);
}
