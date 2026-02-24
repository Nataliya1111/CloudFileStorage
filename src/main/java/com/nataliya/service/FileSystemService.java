package com.nataliya.service;

import com.nataliya.dto.resource.ResourceResponseDto;
import com.nataliya.util.PathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileSystemService {

    private final ResourceMetadataService resourceMetadataService;
    private final MinioService minioService;

    @Transactional
    public List<ResourceResponseDto> uploadFiles(Long userId, String relativeDirectoryPath, List<MultipartFile> files) {

        List<ResourceResponseDto> resourceResponseDtos = new ArrayList<>();

        for (MultipartFile file : files) {

            String filename = file.getOriginalFilename();
            long filesize = file.getSize();
            String pathFormatted = PathUtil.formatPath(relativeDirectoryPath, false, true);

            UUID objectKey = resourceMetadataService.createMetadata(userId, pathFormatted, filename, filesize);
            minioService.uploadFile(objectKey, file);

            resourceResponseDtos.add(new ResourceResponseDto(
                    pathFormatted,
                    filename,
                    Long.toString(filesize)));
        }

        return resourceResponseDtos;
    }
}
