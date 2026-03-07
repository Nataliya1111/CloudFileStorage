package com.nataliya.service;

import com.nataliya.dto.resource.DownloadResourceDto;
import com.nataliya.dto.resource.ResourceResponseDto;
import com.nataliya.exception.FileAlreadyExistsException;
import com.nataliya.exception.FileStreamingException;
import com.nataliya.exception.PartialUploadException;
import com.nataliya.exception.ResourceConflictException;
import com.nataliya.mapper.ResourceMapper;
import com.nataliya.model.ResourceType;
import com.nataliya.model.entity.Resource;
import com.nataliya.service.archive.ZipStreamWriter;
import com.nataliya.util.PathUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileSystemService {

    private final ResourceMetadataService resourceMetadataService;
    private final FileUploadService fileUploadService;
    private final ResourceMapper resourceMapper;
    private final MinioService minioService;

    @Transactional(readOnly = true)
    public ResourceResponseDto getResourceInfo(Long userId, String resourcePath) {

        String normalizedPath = PathUtil.normalizePath(resourcePath);

        Resource resource = resourceMetadataService.getResource(userId, normalizedPath);
        return resourceMapper.resourceToResourceDto(resource);
    }

    @Transactional(readOnly = true)
    public List<ResourceResponseDto> listDirectoryContents(Long userId, String relativeDirectoryPath) {

        String pathFormatted = PathUtil.formatPath(relativeDirectoryPath, false, true);

        List<Resource> directoryContentsList = resourceMetadataService
                .getDirectoryContentsList(userId, pathFormatted);
        return resourceMapper.resourceListToDtoList(directoryContentsList);
    }

    @Transactional
    public ResourceResponseDto createEmptyDirectory(Long userId, String relativeDirectoryPath) {

        String pathFormatted = PathUtil.formatPath(relativeDirectoryPath, false, true);

        Resource emptyDirectory = resourceMetadataService.createEmptyDirectory(userId, pathFormatted);
        return resourceMapper.resourceToResourceDto(emptyDirectory);
    }

    public List<ResourceResponseDto> uploadFiles(Long userId, String relativeDirectoryPath, List<MultipartFile> files) {

        List<Resource> uploadedResources = new ArrayList<>();
        List<String> failedFilePaths = new ArrayList<>();

        for (MultipartFile file : files) {
            String pathFormatted = PathUtil.formatPath(relativeDirectoryPath, false, true);

            try {
                Resource fileMetadata = fileUploadService
                        .uploadSingleFile(userId, file, pathFormatted);
                uploadedResources.add(fileMetadata);
            } catch (FileAlreadyExistsException e) {
                log.info("Attempt to save duplicate file {}", e.getFilePath());
                failedFilePaths.add(e.getFilePath());
            }
        }
        ensureAtLeastOneFileUploaded(uploadedResources);

        if (!failedFilePaths.isEmpty()) {
            throw buildPartialUploadException(files.size(), failedFilePaths);
        }
        return resourceMapper.resourceListToDtoList(uploadedResources);
    }

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

    private void ensureAtLeastOneFileUploaded(List<Resource> uploadedResources) {
        if (uploadedResources.isEmpty()) {
            throw new ResourceConflictException("No files uploaded: all files already exist in the target directory.");
        }
    }

    private PartialUploadException buildPartialUploadException(
            int totalFiles,
            List<String> failedFilePaths) {

        int failedCount = failedFilePaths.size();
        int successCount = totalFiles - failedCount;

        String failedFilesList = String.join("\n", failedFilePaths);

        String message = String.format(
                "Uploaded files - %d. Not uploaded files - %d:%n%s%n" +
                "Files/folders with such names already exist in the target folder",
                successCount,
                failedCount,
                failedFilesList
        );
        return new PartialUploadException(message);
    }
}
