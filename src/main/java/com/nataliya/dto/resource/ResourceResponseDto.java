package com.nataliya.dto.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nataliya.model.ResourceType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResourceResponseDto(
        String path,
        String name,
        String size,
        ResourceType type
) {

    public ResourceResponseDto(String path, String name, String size) {
        this(path, name, size, ResourceType.FILE);
    }

    public ResourceResponseDto(String path, String name) {
        this(path, name, null, ResourceType.DIRECTORY);
    }
}
