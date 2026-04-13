package com.nataliya.config;

import com.nataliya.dto.request.user.UserAuthenticationRequestDto;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer authEndpointCustomizer() {
        return openApi -> {
            Map<String, Schema> schemas =
                    ModelConverters.getInstance()
                            .read(UserAuthenticationRequestDto.class);
            schemas.forEach((name, schema) ->
                    openApi.getComponents().addSchemas(name, schema)
            );
            PathItem signIn = new PathItem().post(new Operation()
                    .addTagsItem("Authentication")
                    .summary("User login")
                    .description("Authenticates the user and creates a session")
                    .requestBody(new RequestBody().content(new Content().addMediaType(
                            "application/json",
                            new MediaType().schema(new Schema<>().$ref("#/components/schemas/UserAuthenticationRequestDto"))
                    )))
                    .responses(new ApiResponses()
                            .addApiResponse("200", new ApiResponse().description("Login successful")
                                    .content(new Content().addMediaType(
                                            "application/json",
                                            new MediaType().schema(new Schema<>().$ref("#/components/schemas/UsernameResponseDto"))
                                    )))
                            .addApiResponse("400", new ApiResponse().description("Validation failed")
                                    .content(new Content().addMediaType(
                                            "application/json",
                                            new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponseDto"))
                                    )))
                            .addApiResponse("401", new ApiResponse().description("Invalid credentials")
                                    .content(new Content().addMediaType(
                                            "application/json",
                                            new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponseDto"))
                                    )))
                            .addApiResponse("500", new ApiResponse().description("Internal server error. An unexpected error occurred while processing the request")
                                    .content(new Content().addMediaType(
                                            "application/json",
                                            new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponseDto"))
                                    )))
                    ));
            openApi.getPaths().addPathItem("/api/auth/sign-in", signIn);
        };
    }

    @Bean
    public OpenApiCustomizer logoutEndpointCustomizer() {
        return openApi -> {
            PathItem logout = new PathItem().post(new Operation()
                    .addTagsItem("Authentication")
                    .summary("User logout")
                    .description("Logs out the current user and invalidates the session")
                    .responses(new ApiResponses()
                            .addApiResponse("204", new ApiResponse().description("Logout successful"))
                            .addApiResponse("401", new ApiResponse().description("Unauthorized. Authentication is required for this request"))
                            .addApiResponse("500", new ApiResponse().description("Internal server error. An unexpected error occurred while processing the request")
                                    .content(new Content().addMediaType(
                                            "application/json",
                                            new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponseDto"))
                                    )))
                    ));
            openApi.getPaths().addPathItem("/api/auth/sign-out", logout);
        };
    }

}
