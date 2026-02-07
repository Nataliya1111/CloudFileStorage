package com.nataliya.dto.resource;

import jakarta.validation.constraints.Pattern;

public record PathRequestDto(
        @Pattern(
                regexp = "^/?([^\\\\/:*?\"<>|]+(/[^\\\\/:*?\"<>|]+)*)?/?$",
                message = "Invalid path. Path format must be valid and path must not contain \\ / : * ? \" < > |"
        )
        String path
) {
}
