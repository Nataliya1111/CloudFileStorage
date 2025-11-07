package com.nataliya.service;

import com.nataliya.dto.UserRegistrationRequestDto;
import com.nataliya.dto.UsernameResponseDto;
import com.nataliya.exception.UserAlreadyExistsException;
import com.nataliya.model.Role;
import com.nataliya.model.User;
import com.nataliya.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UsernameResponseDto register(UserRegistrationRequestDto userRequestDto) {

        User user = User.builder()
                .username(userRequestDto.username())
                .password(passwordEncoder.encode(userRequestDto.password()))
                .role(Role.USER)
                .build();

        try {
            User savedUser = userRepository.save(user);
            return new UsernameResponseDto(savedUser.getUsername());
        } catch (DataIntegrityViolationException ex) {
            if (ex.getCause() instanceof ConstraintViolationException) {
                throw new UserAlreadyExistsException("User with such login already exists", ex);
            } else {
                throw ex;
            }
        }
    }

}
