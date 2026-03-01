package com.nataliya.service;

import com.nataliya.exception.FileAlreadyExistsException;
import com.nataliya.model.entity.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final ResourceMetadataService resourceMetadataService;
    private final MinioService minioService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Resource uploadSingleFile(Long userId, MultipartFile file, String relativeDirectoryPath)
            throws FileAlreadyExistsException {

        String filename = file.getOriginalFilename();
        long filesize = file.getSize();

        Resource fileMetadata = resourceMetadataService.createFileMetadata(userId, relativeDirectoryPath, filename, filesize);
        minioService.uploadFile(fileMetadata.getId(), file);

        return fileMetadata;
    }
}
