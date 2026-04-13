package com.nataliya.controller.openapi;

import com.nataliya.dto.response.error.ErrorResponseDto;
import com.nataliya.dto.request.resource.MovingResourceRequestDto;
import com.nataliya.dto.request.resource.ResourceRequestDto;
import com.nataliya.dto.response.resource.ResourceResponseDto;
import com.nataliya.openapi.annotations.BadRequestAndUnauthorizedResponses;
import com.nataliya.openapi.annotations.CommonStorageErrorResponses;
import com.nataliya.openapi.annotations.InternalServerErrorResponse;
import com.nataliya.security.model.AuthenticatedUser;
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
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@Tag(name = "Resources", description = "Operations related to resources (files and directories)")
@InternalServerErrorResponse
public interface ResourceOpenApi {

    @Operation(summary = "Get resource information by path")
    @ApiResponse(
            responseCode = "200",
            description = "Resource information retrieved successfully"
    )
    @CommonStorageErrorResponses
    ResourceResponseDto resourceInfo(AuthenticatedUser user,
                                     @Valid ResourceRequestDto resourceRequestDto);

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
    List<ResourceResponseDto> upload(AuthenticatedUser user,
                                     @Parameter(description = "Upload target path", example = "folder1/folder2/")
                                     @Pattern(
                                             regexp = "^/?([^\\\\/:*?\"<>|]+(/[^\\\\/:*?\"<>|]+)*)?/?$",
                                             message = "Invalid path. Path format must be valid and path must not contain \\ / : * ? \" < > |"
                                     )
                                     String path,
                                     @Parameter(description = "Files to upload")
                                     List<MultipartFile> objects);

    @Operation(summary = "Delete file or directory")
    @ApiResponse(
            responseCode = "204",
            description = "File or directory deleted successfully"
    )
    @CommonStorageErrorResponses
    void delete(AuthenticatedUser user,
                @Valid ResourceRequestDto resourceRequestDto);

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
    ResponseEntity<StreamingResponseBody> download(
            AuthenticatedUser user,
            @Valid ResourceRequestDto resourceRequestDto);

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
    ResourceResponseDto move(AuthenticatedUser user,
                             @Valid MovingResourceRequestDto requestDto);

    @Operation(
            summary = "Search files and directories by partial name",
            description = "Returns a list of files and directories whose names contain the given query string"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Search results retrieved successfully"
    )
    @BadRequestAndUnauthorizedResponses
    List<ResourceResponseDto> search(AuthenticatedUser user,
                                     @RequestParam @NotBlank String query);
}
