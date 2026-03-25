package com.nataliya.dto.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

public record PathRequestDto(
        @Pattern(
                regexp = "^/?([^\\\\/:*?\"<>|]+(/[^\\\\/:*?\"<>|]+)*)?/?$",
                message = "Invalid path. Path format must be valid and path must not contain \\ / : * ? \" < > |"
        )
        @Schema(description = "Relative path to resource",
                example = "folder1/folder2/"
        )
        String path
) {
}
