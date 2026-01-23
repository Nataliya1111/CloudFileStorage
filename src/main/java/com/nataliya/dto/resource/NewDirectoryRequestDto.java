package com.nataliya.dto.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record NewDirectoryRequestDto(
        @NotBlank(message = "Path must not be empty")
        @Pattern(
                regexp = "^(/?[a-zA-Z0-9._-]+)+/?$",
                message = "Invalid path. Path format must be valid and path can contain only latin letters, digits, '.', '_' or '-'"
        )
        String path) {
}
