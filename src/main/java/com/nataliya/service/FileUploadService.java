package com.nataliya.service;

import com.nataliya.dto.resource.ResourceResponseDto;
import com.nataliya.exception.FileAlreadyExistsException;
import com.nataliya.util.PathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final ResourceMetadataService resourceMetadataService;
    private final MinioService minioService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResourceResponseDto uploadSingleFile(Long userId, MultipartFile file, String relativeDirectoryPath)
            throws FileAlreadyExistsException {

        String filename = file.getOriginalFilename();
        long filesize = file.getSize();
        String pathFormatted = PathUtil.formatPath(relativeDirectoryPath, false, true);

        UUID objectKey = resourceMetadataService.createFileMetadata(userId, pathFormatted, filename, filesize);
        minioService.uploadFile(objectKey, file);

        return new ResourceResponseDto(
                pathFormatted,
                filename,
                Long.toString(filesize));
    }
}
