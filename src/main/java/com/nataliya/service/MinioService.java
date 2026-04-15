package com.nataliya.service;

import com.nataliya.config.MinioProperties;
import com.nataliya.exception.MinioStorageException;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MinioService implements ObjectStorageService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    @Override
    public void uploadFile(UUID objectKey, MultipartFile file) {

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(objectKey.toString())
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception e) {
            throw new MinioStorageException("Failed to upload resource to MinIO storage", e);
        }
    }

    @Override
    public InputStream getFileStream(UUID objectKey) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(objectKey.toString())
                    .build());
        } catch (Exception e) {
            throw new MinioStorageException("Failed to download resource from MinIO storage", e);
        }
    }

    @Override
    public void deleteFiles(List<UUID> objectKeys) {

        if (objectKeys.isEmpty()) {
            return;
        }

        List<DeleteObject> objects = new ArrayList<>();

        for (UUID objectKey : objectKeys) {
            objects.add(new DeleteObject(objectKey.toString()));
        }

        Iterable<Result<DeleteError>> results =
                minioClient.removeObjects(
                        RemoveObjectsArgs.builder()
                                .bucket(properties.getBucketName())
                                .objects(objects)
                                .build());

        throwIfDeleteErrors(results);
    }

    private void throwIfDeleteErrors(Iterable<Result<DeleteError>> errorResults) {
        List<String> errors = new ArrayList<>();

        for (Result<DeleteError> result : errorResults) {
            DeleteError error;

            try {
                error = result.get();
            } catch (Exception e) {
                throw new MinioStorageException("Failed to delete objects from MinIO storage", e);
            }
            errors.add(
                    String.format("%s: (%s)", error.objectName(), error.message())
            );
        }

        if (!errors.isEmpty()) {
            throw new MinioStorageException(
                    "Errors occurred while deleting objects: " + String.join(", ", errors)
            );
        }
    }
}
