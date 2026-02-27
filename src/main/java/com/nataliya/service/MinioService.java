package com.nataliya.service;

import com.nataliya.config.MinioProperties;
import com.nataliya.dto.resource.ResourceResponseDto;
import com.nataliya.exception.MinioStorageException;
import com.nataliya.util.PathUtil;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties properties;
    private String bucketName;

    @PostConstruct
    public void init() {
        this.bucketName = properties.getBucketName();
    }

    public ResourceResponseDto getResourceInfo(Long id, String relativeResourcePath) {

//        String prefix = PathUtil.getFullResourcePath(properties.getUserRootDirectory(), id, relativeResourcePath);
//        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
//                .bucket(bucketName)
//                .prefix(prefix)
//                .includeUserMetadata(true)
//                .maxKeys(1)
//                .build()
//        );
//        if (!results.iterator().hasNext()) {
//            throw new ResourceNotFoundException(String.format("Resource '%s' not found", relativeResourcePath));
//        }
//        if (isDirectory(relativeResourcePath)) {
//            return new ResourceResponseDto(
//                    PathUtil.getParentDirectoryPath(relativeResourcePath),
//                    PathUtil.getResourceName(relativeResourcePath, false)
//            );
//        } else {
//            try {
//                Item item = results.iterator().next().get();
//
//                Map<String, String> meta = getFileMetadata(item);
//                return new ResourceResponseDto(
//                        PathUtil.getParentDirectoryPath(relativeResourcePath),
//                        meta.get(FILENAME_META),
//                        meta.get(SIZE_META)
//                );
//            } catch (Exception e) {
//                throw new MinioStorageException("Failed to get resource info from MinIO storage", e);
//            }
//
//        }

        return null;
    }

    public ResourceResponseDto createEmptyDirectory(Long id, String relativeDirectoryPath) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(PathUtil.getFullDirectoryPath(properties.getUserRootDirectory(), id, relativeDirectoryPath))
                    .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                    .build());
            return new ResourceResponseDto(
                    PathUtil.extractParentDirectoryPath(relativeDirectoryPath),
                    PathUtil.extractResourceName(relativeDirectoryPath, false));
        } catch (Exception e) {
            throw new MinioStorageException("Failed to create new folder in MinIO storage", e);
        }
    }

    public void uploadFile(UUID objectKey, MultipartFile file) {

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey.toString())
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception e) {
            throw new MinioStorageException("Failed to upload resource to MinIO storage", e);
        }
    }

}
