package com.nataliya.dto.response.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nataliya.model.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResourceResponseDto(
        @Schema(
                description = "Path to folder, where resource is",
                example = "folder1/folder2"
        )
        String path,
        @Schema(
                description = "Resource name",
                example = "file.txt"
        )
        String name,
        @Schema(
                description = "Resource size",
                example = "1024"
        )
        Long size,
        @Schema(
                description = "Resource type - DIRECTORY or FILE",
                example = "FILE"
        )
        ResourceType type
) {
}
