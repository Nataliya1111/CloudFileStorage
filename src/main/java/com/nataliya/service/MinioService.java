package com.nataliya.service;

import com.nataliya.config.MinioProperties;
import com.nataliya.dto.resource.ResourceResponseDto;
import com.nataliya.exception.MinioStorageException;
import com.nataliya.exception.ResourceNotFoundException;
import com.nataliya.util.PathUtil;
import io.minio.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MinioService {

    public static final String FILENAME_META = "x-amz-meta-filename";
    public static final String SIZE_META = "x-amz-meta-size";


    private final MinioClient minioClient;
    private final MinioProperties properties;
    private String bucketName;

    @PostConstruct
    public void init() {
        this.bucketName = properties.getBucketName();
    }

    public void createUserRootDirectory(Long id) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(String.format(properties.getUserRootDirectory(), id))
                    .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                    .build());
        } catch (Exception e) {
            throw new MinioStorageException("Failed to create user folder in MinIO storage", e);
        }
    }

    public ResourceResponseDto getResourceInfo(Long id, String relativeResourcePath) {

        String prefix = PathUtil.getFullResourcePath(properties.getUserRootDirectory(), id, relativeResourcePath);
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .includeUserMetadata(true)
                .maxKeys(1)
                .build()
        );
        if (!results.iterator().hasNext()) {
            throw new ResourceNotFoundException(String.format("Resource '%s' not found", relativeResourcePath));
        }
        if (isDirectory(relativeResourcePath)) {
            return new ResourceResponseDto(
                    PathUtil.getParentDirectoryPath(relativeResourcePath),
                    PathUtil.getResourceName(relativeResourcePath, false)
            );
        } else {
            try {
                Item item = results.iterator().next().get();

                Map<String, String> meta = getFileMetadata(item);
                return new ResourceResponseDto(
                        PathUtil.getParentDirectoryPath(relativeResourcePath),
                        meta.get(FILENAME_META),
                        meta.get(SIZE_META)
                );
            } catch (Exception e) {
                throw new MinioStorageException("Failed to get resource info from MinIO storage", e);
            }

        }
    }

    public List<ResourceResponseDto> listDirectoryContents(Long id, String relativeDirectoryPath) {
        String prefix = PathUtil.getFullDirectoryPath(properties.getUserRootDirectory(), id, relativeDirectoryPath);
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .includeUserMetadata(true)
                .recursive(false)
                .build());

        List<ResourceResponseDto> resourcesList = new ArrayList<>();

        for (Result<Item> result : results) {
            try {
                Item item = result.get();
                if (item.objectName().equals(prefix)) {
                    continue;
                }

                String pathFormatted = PathUtil.formatPath(relativeDirectoryPath, false, true);
                String objectName = item.objectName();
                ResourceResponseDto responseDto;
                if (isDirectory(objectName)) {
                    responseDto = new ResourceResponseDto(
                            pathFormatted,
                            PathUtil.getResourceName(objectName, true)
                    );
                } else {
                    Map<String, String> meta = getFileMetadata(item);
                    responseDto = new ResourceResponseDto(
                            pathFormatted,
                            meta.get(FILENAME_META),
                            meta.get(SIZE_META)
                    );
                }
                resourcesList.add(responseDto);
            } catch (Exception e) {
                throw new MinioStorageException("Failed to get directory contents", e);
            }
        }
        return resourcesList;
    }

    public ResourceResponseDto createEmptyDirectory(Long id, String relativeDirectoryPath) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(PathUtil.getFullDirectoryPath(properties.getUserRootDirectory(), id, relativeDirectoryPath))
                    .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                    .build());
            return new ResourceResponseDto(
                    PathUtil.getParentDirectoryPath(relativeDirectoryPath),
                    PathUtil.getResourceName(relativeDirectoryPath, false));
        } catch (Exception e) {
            throw new MinioStorageException("Failed to create new folder in MinIO storage", e);
        }
    }

    public ResourceResponseDto uploadFile(Long id, String relativeDirectoryPath, MultipartFile file) {

        String filename = file.getOriginalFilename();
        String filesize = Long.toString(file.getSize());

        HashMap<String, String> metadata = new HashMap<>();
        metadata.put(FILENAME_META, filename);
        metadata.put(SIZE_META, filesize);

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
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

    private Map<String, String> getFileMetadata(Item item) {
        return item.userMetadata().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue));
    }

    private boolean isDirectory(String path) {
        return path.endsWith("/");
    }
}
