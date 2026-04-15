package com.nataliya.service;

import com.nataliya.exception.FileAlreadyExistsException;
import com.nataliya.exception.StorageLimitExceededException;
import com.nataliya.model.entity.Resource;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final ResourceMetadataService resourceMetadataService;
    private final ObjectStorageService objectStorageService;

    @Value("${app.max-storage.user}")
    private DataSize maxUserStorage;

    @Value("${app.max-storage.server}")
    private DataSize maxServerStorage;

    private long maxUserStorageBytes;
    private long maxServerStorageBytes;

    @PostConstruct
    void init() {
        maxUserStorageBytes = maxUserStorage.toBytes();
        maxServerStorageBytes = maxServerStorage.toBytes();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Resource uploadSingleFile(Long userId, MultipartFile file, String relativeDirectoryPath)
            throws FileAlreadyExistsException {

        String filename = file.getOriginalFilename();
        long filesize = file.getSize();

        checkUserStorageLimit(userId, filesize);
        checkServerStorageLimit(filesize);

        Resource fileMetadata = resourceMetadataService.createFileMetadata(userId, relativeDirectoryPath, filename, filesize);
        objectStorageService.uploadFile(fileMetadata.getId(), file);

        return fileMetadata;
    }

    private void checkUserStorageLimit(Long userId, long filesize) {
        long userStorageUsage = resourceMetadataService.getUserStorageUsage(userId);
        if (userStorageUsage + filesize > maxUserStorageBytes) {
            throw new StorageLimitExceededException("User storage limit exceeded");
        }
    }

    private void checkServerStorageLimit(long filesize) {
        long totalStorageUsage = resourceMetadataService.getTotalStorageUsage();
        if (totalStorageUsage + filesize > maxServerStorageBytes) {
            throw new StorageLimitExceededException("Server storage full");
        }
    }
}
