package com.nataliya.integration.service;

import com.nataliya.dto.user.UserRegistrationRequestDto;
import com.nataliya.exception.UserAlreadyExistsException;
import com.nataliya.model.User;
import com.nataliya.repository.UserRepository;
import com.nataliya.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
public class RegistrationServiceIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RegistrationService registrationService;

    @ParameterizedTest
    @CsvSource({
            "TestName1, Password1",
            "TestName1_TestName11, Pass123@#$",
            "User3, D1234"
    })
    void registerUser_whenValidData_thenCreatesUser(String username, String password) {

        UserRegistrationRequestDto userRequestDto = new UserRegistrationRequestDto(username, password);
        registrationService.registerUser(userRequestDto);

        Optional<User> actualResult = userRepository.findByUsername(username);

        assertThat(actualResult).isPresent()
                .get()
                .satisfies((user) -> {
                    assertThat(user.getId()).isNotNull();
                    assertThat(user.getUsername()).isEqualTo(username);
                });
    }

    @Test
    void registerUser_whenDuplicateLogin_thenThrowsException() {

        String username = "TestName1";

        UserRegistrationRequestDto userRequestDto = new UserRegistrationRequestDto(username, "Password1");
        registrationService.registerUser(userRequestDto);

        assertThatThrownBy(() -> registrationService
                .registerUser(new UserRegistrationRequestDto(username, "Pass123@#$")))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

}
