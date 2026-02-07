package com.nataliya.controller;

import com.nataliya.dto.resource.NewDirectoryRequestDto;
import com.nataliya.dto.resource.PathRequestDto;
import com.nataliya.dto.resource.ResourceResponseDto;
import com.nataliya.security.model.AuthenticatedUser;
import com.nataliya.service.MinioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final MinioService minioService;

    @GetMapping
    public List<ResourceResponseDto> listDirectoryContents(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid PathRequestDto pathRequestDto) {

        return minioService.listDirectoryContents(user.getId(), pathRequestDto.path());
    }

    @PostMapping
    public ResponseEntity<ResourceResponseDto> createEmptyDirectory(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid NewDirectoryRequestDto directoryRequestDto) {

        ResourceResponseDto responseDto = minioService.createEmptyDirectory(user.getId(), directoryRequestDto.path());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseDto);
    }

}
