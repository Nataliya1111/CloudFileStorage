package com.nataliya.service;

import com.nataliya.config.MinioProperties;
import com.nataliya.dto.resource.ResourceResponseDto;
import com.nataliya.exception.MinioStorageException;
import com.nataliya.util.PathUtil;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    public void createUserRootDirectory(Long id) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(String.format(properties.getUserRootDirectory(), id))
                    .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                    .build());
        } catch (Exception e) {
            throw new MinioStorageException("Failed to create user folder in MinIO storage", e);
        }
    }

    public ResourceResponseDto createEmptyDirectory(Long id, String relativeDirectoryPath) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(PathUtil.getFullDirectoryPath(properties.getUserRootDirectory(), id, relativeDirectoryPath))
                    .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                    .build());
            return new ResourceResponseDto(
                    PathUtil.getParentDirectoryPath(relativeDirectoryPath),
                    PathUtil.getDirectoryName(relativeDirectoryPath));
        } catch (Exception e) {
            throw new MinioStorageException("Failed to create new folder in MinIO storage", e);
        }
    }

    public ResourceResponseDto uploadFile(Long id, String relativeDirectoryPath, MultipartFile file) {

        String filename = file.getOriginalFilename();
        String filesize = Long.toString(file.getSize());

        HashMap<String, String> metadata = new HashMap<>();
        metadata.put("filename", filename);
        metadata.put("size", filesize);

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(PathUtil.buildFullObjectName(properties.getUserRootDirectory(), id, relativeDirectoryPath))
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .userMetadata(metadata)
                    .build());
            return new ResourceResponseDto(
                    PathUtil.formatPath(relativeDirectoryPath, false, true),
                    filename,
                    filesize);
        } catch (Exception e) {
            throw new MinioStorageException("Failed to upload resource to MinIO storage", e);
        }
    }
}
