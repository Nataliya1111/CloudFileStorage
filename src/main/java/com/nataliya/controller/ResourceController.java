package com.nataliya.controller;

import com.nataliya.dto.resource.PathRequestDto;
import com.nataliya.dto.resource.ResourceRequestDto;
import com.nataliya.dto.resource.ResourceResponseDto;
import com.nataliya.security.model.AuthenticatedUser;
import com.nataliya.service.MinioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final MinioService minioService;

    @GetMapping
    public ResourceResponseDto resourceInfo(@AuthenticationPrincipal AuthenticatedUser user,
                                            @Valid ResourceRequestDto resourceRequestDto) {


        return minioService.getResourceInfo(user.getId(), resourceRequestDto.path());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceResponseDto upload(@AuthenticationPrincipal AuthenticatedUser user,
                                      @Valid PathRequestDto pathRequestDto,
                                      @RequestPart MultipartFile object) {

        return minioService.uploadFile(user.getId(), pathRequestDto.path(), object);
    }
}
