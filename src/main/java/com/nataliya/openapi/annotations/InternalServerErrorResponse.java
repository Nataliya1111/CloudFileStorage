package com.nataliya.openapi.annotations;


import com.nataliya.dto.error.ErrorResponseDto;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
        responseCode = "500",
        description = "Internal server error. An unexpected error occurred while processing the request",
        content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
)
public @interface InternalServerErrorResponse {
}
