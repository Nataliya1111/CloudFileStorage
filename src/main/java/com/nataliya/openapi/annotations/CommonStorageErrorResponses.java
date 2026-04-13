package com.nataliya.openapi.annotations;

import com.nataliya.dto.response.error.ErrorResponseDto;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request. For example, the path does not follow the expected format",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Authentication is required to access this resource",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Resource not found. The requested file or directory does not exist",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
})
public @interface CommonStorageErrorResponses {
}
