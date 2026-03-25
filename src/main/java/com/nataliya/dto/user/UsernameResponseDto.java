package com.nataliya.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

public record UsernameResponseDto(

        @Schema(example = "User1")
        String username
) {
}
