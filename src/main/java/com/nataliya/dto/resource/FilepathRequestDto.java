package com.nataliya.dto.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record FilepathRequestDto(
        @NotBlank(message = "Path must not be empty")
        @Pattern(
                regexp = "^/?([^\\\\/:*?\"<>|]+(/[^\\\\/:*?\"<>|]+)*)?/?$",
                message = "Invalid path. Path format must be valid and path must not contain \\ / : * ? \" < > |"
        )
        String path
) {
}
