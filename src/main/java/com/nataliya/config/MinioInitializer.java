package com.nataliya.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinioInitializer {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    @PostConstruct
    public void createBucketIfNotExists() {
        try {
            if (!bucketExists()) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(properties.getBucketName())
                                .build());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize MinIO bucket", e);
        }
    }

    private boolean bucketExists() throws Exception {
        return minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(properties.getBucketName())
                        .build());
    }

}
