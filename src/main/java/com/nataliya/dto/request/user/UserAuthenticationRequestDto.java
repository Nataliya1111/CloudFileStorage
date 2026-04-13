package com.nataliya.dto.request.user;

import com.nataliya.validation.ValidPassword;
import com.nataliya.validation.ValidUsername;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserAuthenticationRequestDto(

        @ValidUsername
        @Schema(example = "User1")
        String username,

        @ValidPassword
        @Schema(example = "MyPassword")
        String password
) {
}
