package com.nataliya.controller.openapi;

import com.nataliya.dto.error.ErrorResponseDto;
import com.nataliya.dto.resource.PathRequestDto;
import com.nataliya.dto.resource.ResourceRequestDto;
import com.nataliya.dto.resource.ResourceResponseDto;
import com.nataliya.openapi.annotations.CommonStorageErrorResponses;
import com.nataliya.openapi.annotations.InternalServerErrorResponse;
import com.nataliya.security.model.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

@Tag(name = "Directories", description = "Operations related to directories")
@InternalServerErrorResponse
public interface DirectoryOpenApi {

    @Operation(summary = "List directory contents")
    @ApiResponse(
            responseCode = "200",
            description = "Directory contents retrieved successfully"
    )
    @CommonStorageErrorResponses
    List<ResourceResponseDto> listDirectoryContents(
            AuthenticatedUser user,
            @Valid PathRequestDto pathRequestDto);

    @Operation(
            summary = "Create an empty directory",
            description = "Creates an empty directory at the specified path"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Directory created successfully"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A file or directory with the same name already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @CommonStorageErrorResponses
    ResourceResponseDto createEmptyDirectory(
            AuthenticatedUser user,
            @Valid ResourceRequestDto newDirectoryRequestDto);
}
