package com.nataliya.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponseDto(

        @Schema(example = "Unexpected error")
        String message
) {
}
