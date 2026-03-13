package com.nataliya.service;

import com.nataliya.dto.resource.DownloadResourceDto;
import com.nataliya.exception.FileStreamingException;
import com.nataliya.model.ResourceType;
import com.nataliya.model.entity.Resource;
import com.nataliya.service.archive.ZipStreamWriter;
import com.nataliya.util.PathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DownloadService {

    private final ResourceMetadataService resourceMetadataService;
    private final MinioService minioService;

    public DownloadResourceDto prepareDownload(Long userId, String resourcePath) {

        String normalizedPath = PathUtil.normalizePath(resourcePath);

        Resource resource = resourceMetadataService.getResource(userId, normalizedPath);
        String downloadedResourceName = resource.getResourceType() == ResourceType.FILE ?
                resource.getResourceName() : (resource.getResourceName() + ".zip");

        StreamingResponseBody body =
                outputStream -> writeDataToStream(userId, normalizedPath, outputStream);

        return new DownloadResourceDto(downloadedResourceName, body);
    }

    private void writeDataToStream(Long userId, String resourcePath, OutputStream outputStream) {

        if (resourcePath.endsWith("/")) {
            writeDirectoryContentsToStream(userId, resourcePath, outputStream);
        } else {
            writeFileToStream(userId, resourcePath, outputStream);
        }
    }

    private void writeFileToStream(Long userId, String resourcePath, OutputStream outputStream) {

        Resource file = resourceMetadataService.getResource(userId, resourcePath);
        ensureIsFile(file);
        try (InputStream inputStream = minioService.getFileStream(file.getId())) {
            inputStream.transferTo(outputStream);
        } catch (IOException e) {
            throw new FileStreamingException("Failed to stream file: " + resourcePath, e);
        }
    }

    private void writeDirectoryContentsToStream(Long userId, String resourcePath, OutputStream outputStream) {

        List<Resource> subtree = resourceMetadataService.getDirectorySubtree(userId, resourcePath);

        Set<UUID> resourcesThatAreParentsIds = subtree.stream()
                .map(Resource::getParent)
                .filter(Objects::nonNull)
                .map(Resource::getId)
                .collect(Collectors.toSet());

        List<Resource> leafs = subtree.stream()
                .filter(r -> !resourcesThatAreParentsIds.contains(r.getId()))
                .toList();

        try (ZipStreamWriter zipWriter = new ZipStreamWriter(outputStream)) {

            for (Resource resource : leafs) {
                String zipPath = PathUtil.extractZipPath(resource.getPath(), resourcePath);
                if (resource.getResourceType() == ResourceType.FILE) {
                    try (InputStream inputStream = minioService.getFileStream(resource.getId())) {
                        zipWriter.addFile(zipPath, inputStream);
                    }
                } else {
                    zipWriter.addDirectory(zipPath);
                }
            }
        } catch (IOException e) {
            throw new FileStreamingException("Failed to stream file: " + resourcePath, e);
        }
    }

    private void ensureIsFile(Resource resource) {
        if (resource.getResourceType() != ResourceType.FILE) {
            throw new IllegalStateException(
                    "Expected FILE resource but got DIRECTORY for path: " + resource.getPath()
            );
        }
    }
}
