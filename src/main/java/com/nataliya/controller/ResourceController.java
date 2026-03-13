package com.nataliya.controller;

import com.nataliya.dto.resource.*;
import com.nataliya.security.model.AuthenticatedUser;
import com.nataliya.service.FileSystemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
@Validated
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

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthenticatedUser user,
                       @Valid ResourceRequestDto resourceRequestDto) {

        fileSystemService.deleteResource(user.getId(), resourceRequestDto.path());
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

    @GetMapping("/move")
    public ResourceResponseDto move(@AuthenticationPrincipal AuthenticatedUser user,
                                    @Valid MovingResourceRequestDto requestDto) {

        return fileSystemService.move(user.getId(), requestDto.from(), requestDto.to());
    }

    @GetMapping("/search")
    public List<ResourceResponseDto> search(@AuthenticationPrincipal AuthenticatedUser user,
                                            @RequestParam @NotBlank String query) {

        return fileSystemService.search(user.getId(), query);
    }
}
