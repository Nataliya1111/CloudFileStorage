package com.nataliya.controller;

import com.nataliya.controller.openapi.ResourceOpenApi;
import com.nataliya.dto.DownloadResourceDto;
import com.nataliya.dto.request.resource.MovingResourceRequestDto;
import com.nataliya.dto.request.resource.ResourceRequestDto;
import com.nataliya.dto.response.resource.ResourceResponseDto;
import com.nataliya.security.model.AuthenticatedUser;
import com.nataliya.service.FileSystemService;
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
public class ResourceController implements ResourceOpenApi {

    private final FileSystemService fileSystemService;

    @Override
    @GetMapping
    public ResourceResponseDto resourceInfo(@AuthenticationPrincipal AuthenticatedUser user,
                                            ResourceRequestDto resourceRequestDto) {

        return fileSystemService.getResourceInfo(user.getId(), resourceRequestDto.path());
    }

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<ResourceResponseDto> upload(@AuthenticationPrincipal AuthenticatedUser user,
                                            @RequestParam("path") String path,
                                            @RequestPart("object") List<MultipartFile> objects) {

        return fileSystemService.uploadFiles(user.getId(), path, objects);
    }

    @Override
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthenticatedUser user,
                       ResourceRequestDto resourceRequestDto) {

        fileSystemService.deleteResource(user.getId(), resourceRequestDto.path());
    }

    @Override
    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> download(@AuthenticationPrincipal AuthenticatedUser user,
                                                          ResourceRequestDto resourceRequestDto) {

        DownloadResourceDto download = fileSystemService.download(user.getId(), resourceRequestDto.path());

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

    @Override
    @GetMapping("/move")
    public ResourceResponseDto move(@AuthenticationPrincipal AuthenticatedUser user,
                                    MovingResourceRequestDto requestDto) {

        return fileSystemService.move(user.getId(), requestDto.from(), requestDto.to());
    }

    @Override
    @GetMapping("/search")
    public List<ResourceResponseDto> search(@AuthenticationPrincipal AuthenticatedUser user,
                                            String query) {

        return fileSystemService.search(user.getId(), query);
    }
}
