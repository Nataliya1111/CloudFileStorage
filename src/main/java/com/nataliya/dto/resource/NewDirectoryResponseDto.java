package com.nataliya.dto.resource;

public record NewDirectoryResponseDto(
        String path,
        String name,
        String type
) {
    public NewDirectoryResponseDto(String path, String name) {
        this(path, name, "DIRECTORY");
    }
}
