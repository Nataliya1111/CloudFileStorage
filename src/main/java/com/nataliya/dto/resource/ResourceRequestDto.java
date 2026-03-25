package com.nataliya.dto.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResourceRequestDto(
        @NotBlank(message = "Path must not be empty")
        @Pattern(
                regexp = "^/?[^\\\\/:*?\"<>|]+(/[^\\\\/:*?\"<>|]+)*/?$",
                message = "Invalid path. Path format must be valid and path must not contain \\ / : * ? \" < > |"
        )
        @Schema(
                description = "Full path to resource",
                example = "folder/file.txt"
        )
        String path
) {
}
