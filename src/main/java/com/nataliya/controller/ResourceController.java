package com.nataliya.controller;

import com.nataliya.dto.error.ErrorResponseDto;
import com.nataliya.dto.resource.*;
import com.nataliya.openapi.annotations.BadRequestAndUnauthorizedResponses;
import com.nataliya.openapi.annotations.CommonStorageErrorResponses;
import com.nataliya.openapi.annotations.InternalServerErrorResponse;
import com.nataliya.security.model.AuthenticatedUser;
import com.nataliya.service.FileSystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
@Validated
@Tag(name = "Resources", description = "Operations related to resources (files and directories)")
@InternalServerErrorResponse
public class ResourceController {

    private final FileSystemService fileSystemService;

    @Operation(summary = "Get resource information by path")
    @ApiResponse(
            responseCode = "200",
            description = "Resource information retrieved successfully"
    )
    @CommonStorageErrorResponses
    @GetMapping
    public ResourceResponseDto resourceInfo(@AuthenticationPrincipal AuthenticatedUser user,
                                            @Valid ResourceRequestDto resourceRequestDto) {

        return fileSystemService.getResourceInfo(user.getId(), resourceRequestDto.path());
    }

    @Operation(
            summary = "Upload files and directories",
            description = "Uploads one or more files and directories (recursively) to the specified path"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Files and directories uploaded successfully"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A file or directory with the same name already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "413",
                    description = "Payload too large. The file exceeds size limits or storage capacity has been reached",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @BadRequestAndUnauthorizedResponses
    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ResourceResponseDto> upload(@AuthenticationPrincipal AuthenticatedUser user,
                                            @Parameter(description = "Upload target path", example = "folder1/folder2/")
                                            @Pattern(
                                                    regexp = "^/?([^\\\\/:*?\"<>|]+(/[^\\\\/:*?\"<>|]+)*)?/?$",
                                                    message = "Invalid path. Path format must be valid and path must not contain \\ / : * ? \" < > |"
                                            )
                                            @RequestParam("path") String path,
                                            @Parameter(description = "Files to upload")
                                            @RequestPart("object") List<MultipartFile> objects) {

        return fileSystemService.uploadFiles(user.getId(), path, objects);
    }

    @Operation(summary = "Delete file or directory")
    @ApiResponse(
            responseCode = "204",
            description = "File or directory deleted successfully"
    )
    @CommonStorageErrorResponses
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthenticatedUser user,
                       @Valid ResourceRequestDto resourceRequestDto) {

        fileSystemService.deleteResource(user.getId(), resourceRequestDto.path());
    }

    @Operation(
            summary = "Download file or directory",
            description = "Downloads a file as a binary stream or a directory as a ZIP archive"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Download completed successfully",
            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    )
    @CommonStorageErrorResponses
    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> download(@AuthenticationPrincipal AuthenticatedUser user,
                                                          @Valid ResourceRequestDto resourceRequestDto) {

        DownloadResourceDto download = fileSystemService.download(user.getId(), resourceRequestDto.path());

        ContentDisposition disposition =
                ContentDisposition.attachment()
                        .filename(download.filename(),
                                StandardCharsets.UTF_8)
                        .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(download.body());
    }

    @Operation(
            summary = "Move or rename a file or directory",
            description = "Moves or renames a file or directory. " +
                          "The operation supports either moving or renaming, but not both at the same time"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "File or directory moved or renamed successfully"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A file or directory with the same name already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @CommonStorageErrorResponses
    @GetMapping("/move")
    public ResourceResponseDto move(@AuthenticationPrincipal AuthenticatedUser user,
                                    @Valid MovingResourceRequestDto requestDto) {

        return fileSystemService.move(user.getId(), requestDto.from(), requestDto.to());
    }

    @Operation(
            summary = "Search files and directories by partial name",
            description = "Returns a list of files and directories whose names contain the given query string"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Search results retrieved successfully"
    )
    @BadRequestAndUnauthorizedResponses
    @GetMapping("/search")
    public List<ResourceResponseDto> search(@AuthenticationPrincipal AuthenticatedUser user,
                                            @RequestParam @NotBlank String query) {

        return fileSystemService.search(user.getId(), query);
    }
}
