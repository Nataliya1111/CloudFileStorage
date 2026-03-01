package com.nataliya.controller;

import com.nataliya.dto.resource.ResourceRequestDto;
import com.nataliya.dto.resource.PathRequestDto;
import com.nataliya.dto.resource.ResourceResponseDto;
import com.nataliya.security.model.AuthenticatedUser;
import com.nataliya.service.FileSystemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final FileSystemService fileSystemService;

    @GetMapping
    public List<ResourceResponseDto> listDirectoryContents(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid PathRequestDto pathRequestDto) {

        return fileSystemService.listDirectoryContents(user.getId(), pathRequestDto.path());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceResponseDto createEmptyDirectory(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid ResourceRequestDto newDirectoryRequestDto) {

        return fileSystemService.createEmptyDirectory(user.getId(), newDirectoryRequestDto.path());
    }
}
