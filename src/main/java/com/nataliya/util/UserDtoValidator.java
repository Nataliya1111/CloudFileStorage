package com.nataliya.util;

import com.nataliya.dto.UserAuthenticationRequestDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class UserDtoValidator {

    public void validate (@Valid UserAuthenticationRequestDto userRequestDto) {

    }
}
