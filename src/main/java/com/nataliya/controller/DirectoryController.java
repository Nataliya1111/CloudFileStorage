package com.nataliya.controller;

import com.nataliya.controller.openapi.DirectoryOpenApi;
import com.nataliya.dto.resource.ResourceRequestDto;
import com.nataliya.dto.resource.PathRequestDto;
import com.nataliya.dto.resource.ResourceResponseDto;
import com.nataliya.security.model.AuthenticatedUser;
import com.nataliya.service.FileSystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController implements DirectoryOpenApi {

    private final FileSystemService fileSystemService;

    @Override
    @GetMapping
    public List<ResourceResponseDto> listDirectoryContents(
            @AuthenticationPrincipal AuthenticatedUser user,
            PathRequestDto pathRequestDto) {

        return fileSystemService.listDirectoryContents(user.getId(), pathRequestDto.path());
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceResponseDto createEmptyDirectory(
            @AuthenticationPrincipal AuthenticatedUser user,
            ResourceRequestDto newDirectoryRequestDto) {

        return fileSystemService.createEmptyDirectory(user.getId(), newDirectoryRequestDto.path());
    }

}
