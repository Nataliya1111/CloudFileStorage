package com.nataliya.dto.resource;

import com.nataliya.validation.ValidMovePath;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@ValidMovePath
public record MovingResourceRequestDto(
        @NotBlank(message = "Path must not be empty")
        @Pattern(
                regexp = "^/?[^\\\\/:*?\"<>|]+(/[^\\\\/:*?\"<>|]+)*/?$",
                message = "Invalid path. Path format must be valid and path must not contain \\ / : * ? \" < > |"
        )
        String from,

        @NotBlank(message = "Path must not be empty")
        @Pattern(
                regexp = "^/?[^\\\\/:*?\"<>|]+(/[^\\\\/:*?\"<>|]+)*/?$",
                message = "Invalid path. Path format must be valid and path must not contain \\ / : * ? \" < > |"
        )
        String to
) {
}
