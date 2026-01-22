package com.nataliya.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserAuthenticationRequestDto(
        @NotBlank(message = "Username must not be empty")
        @Size(
                min = 5, max = 20,
                message = "Username must be {min} to {max} characters long"
        )
        @Pattern(
                regexp = "^[a-zA-Z0-9]+[a-zA-Z_0-9]*[a-zA-Z0-9]+$",
                message = "Username can contain only latin letters, digits or underscores (not the first or the last character)"
        )
        String username,

        @NotBlank(message = "Password must not be empty")
        @Size(
                min = 5, max = 20,
                message = "Password must be {min} to {max} characters long"
        )
        @Pattern(
                regexp = "^[a-zA-Z0-9!@#$%^&*(),.?\":{}|<>\\\\/`~+=-_';]*$",
                message = "Password contains invalid characters. Only Latin letters, digits and common symbols are allowed"
        )
        String password
) {
}
