package com.nataliya.service;

import com.nataliya.config.MinioProperties;
import com.nataliya.exception.MinioStorageException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    public void createUserFolder(Long id) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(String.format(properties.getUserFolderName(), id))
                    .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                    .build());
        } catch (Exception e) {
            throw new MinioStorageException("Failed to create user folder in MinIO storage", e);
        }
    }
}
