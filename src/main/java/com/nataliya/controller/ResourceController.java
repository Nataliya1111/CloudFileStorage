package com.nataliya.controller;

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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final MinioService minioService;

    @PostMapping
    public ResponseEntity<ResourceResponseDto> upload(@AuthenticationPrincipal AuthenticatedUser user,
                                                      @Valid PathRequestDto pathRequestDto,
                                                      @RequestPart MultipartFile file) {

        ResourceResponseDto responseDto = minioService.uploadFile(user.getId(), pathRequestDto.path(), file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseDto);
    }
}
