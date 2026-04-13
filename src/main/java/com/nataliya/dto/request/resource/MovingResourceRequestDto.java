package com.nataliya.dto.request.resource;

import com.nataliya.validation.ValidMovePath;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@ValidMovePath
public record MovingResourceRequestDto(
        @NotBlank(message = "Path must not be empty")
        @Pattern(
                regexp = "^/?[^\\\\/:*?\"<>|]+(/[^\\\\/:*?\"<>|]+)*/?$",
                message = "Invalid path. Path format must be valid and path must not contain \\ / : * ? \" < > |"
        )
        @Schema(
                description = "Source path of the resource to move",
                example = "folder1/file.txt"
        )
        String from,

        @NotBlank(message = "Path must not be empty")
        @Pattern(
                regexp = "^/?[^\\\\/:*?\"<>|]+(/[^\\\\/:*?\"<>|]+)*/?$",
                message = "Invalid path. Path format must be valid and path must not contain \\ / : * ? \" < > |"
        )
        @Schema(
                description = "Destination path of the resource to move",
                example = "folder2/file.txt"
        )
        String to
) {
}
