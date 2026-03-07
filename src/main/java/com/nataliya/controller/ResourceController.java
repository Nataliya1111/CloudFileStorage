package com.nataliya.controller;

import com.nataliya.dto.resource.DownloadResourceDto;
import com.nataliya.dto.resource.PathRequestDto;
import com.nataliya.dto.resource.ResourceRequestDto;
import com.nataliya.dto.resource.ResourceResponseDto;
import com.nataliya.security.model.AuthenticatedUser;
import com.nataliya.service.FileSystemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
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

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> download(@AuthenticationPrincipal AuthenticatedUser user,
                                                          @Valid ResourceRequestDto resourceRequestDto) {

        DownloadResourceDto download = fileSystemService.prepareDownload(user.getId(), resourceRequestDto.path());

        ContentDisposition disposition =
                ContentDisposition.attachment()
                        .filename(download.filename(),
                                StandardCharsets.UTF_8)
                        .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(download.body());
    }
}
