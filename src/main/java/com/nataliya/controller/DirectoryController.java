package com.nataliya.controller;

import com.nataliya.dto.error.ErrorResponseDto;
import com.nataliya.dto.resource.ResourceRequestDto;
import com.nataliya.dto.resource.PathRequestDto;
import com.nataliya.dto.resource.ResourceResponseDto;
import com.nataliya.openapi.annotations.CommonStorageErrorResponses;
import com.nataliya.openapi.annotations.InternalServerErrorResponse;
import com.nataliya.security.model.AuthenticatedUser;
import com.nataliya.service.FileSystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
@Tag(name = "Directories", description = "Operations related to directories")
@InternalServerErrorResponse
public class DirectoryController {

    private final FileSystemService fileSystemService;

    @Operation(summary = "List directory contents")
    @ApiResponse(
            responseCode = "200",
            description = "Directory contents retrieved successfully"
    )
    @CommonStorageErrorResponses
    @GetMapping
    public List<ResourceResponseDto> listDirectoryContents(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid PathRequestDto pathRequestDto) {

        return fileSystemService.listDirectoryContents(user.getId(), pathRequestDto.path());
    }

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
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceResponseDto createEmptyDirectory(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid ResourceRequestDto newDirectoryRequestDto) {

        return fileSystemService.createEmptyDirectory(user.getId(), newDirectoryRequestDto.path());
    }
}
