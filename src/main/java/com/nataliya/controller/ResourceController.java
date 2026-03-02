package com.nataliya.controller;

import com.nataliya.dto.resource.PathRequestDto;
import com.nataliya.dto.resource.ResourceRequestDto;
import com.nataliya.dto.resource.ResourceResponseDto;
import com.nataliya.security.model.AuthenticatedUser;
import com.nataliya.service.FileSystemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final FileSystemService fileSystemService;

    @GetMapping
    public ResourceResponseDto resourceInfo(@AuthenticationPrincipal AuthenticatedUser user,
                                            @Valid ResourceRequestDto resourceRequestDto) {

        return fileSystemService.getResourceInfo(user.getId(), resourceRequestDto.path());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<ResourceResponseDto> upload(@AuthenticationPrincipal AuthenticatedUser user,
                                            @Valid PathRequestDto pathRequestDto,
                                            @RequestPart("object") List<MultipartFile> objects) {

        return fileSystemService.uploadFiles(user.getId(), pathRequestDto.path(), objects);
    }
}
