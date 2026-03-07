package com.nataliya.service;

import com.nataliya.config.MinioProperties;
import com.nataliya.exception.MinioStorageException;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
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

    public InputStream getFileStream(UUID objectKey) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey.toString())
                    .build());
        } catch (Exception e) {
            throw new MinioStorageException("Failed to download resource from MinIO storage", e);
        }
    }
}
