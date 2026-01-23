package com.nataliya.controller;

import com.nataliya.dto.resource.NewDirectoryRequestDto;
import com.nataliya.dto.resource.NewDirectoryResponseDto;
import com.nataliya.security.model.AuthenticatedUser;
import com.nataliya.service.MinioService;
import com.nataliya.util.PathUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DirectoryController {

    private final MinioService minioService;

    @PostMapping("/directory")
    public ResponseEntity<NewDirectoryResponseDto> createEmptyDirectory(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid NewDirectoryRequestDto directoryRequestDto) {

        String relativeDirectoryPath = directoryRequestDto.path();
        minioService.createEmptyDirectory(user.getId(), relativeDirectoryPath);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new NewDirectoryResponseDto(
                        PathUtil.getParentDirectoryPath(relativeDirectoryPath),
                        PathUtil.getDirectoryName(relativeDirectoryPath)
                ));
    }
}
