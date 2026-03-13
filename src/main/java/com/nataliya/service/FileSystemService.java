package com.nataliya.service;

import com.nataliya.dto.resource.DownloadResourceDto;
import com.nataliya.dto.resource.ResourceResponseDto;
import com.nataliya.exception.*;
import com.nataliya.mapper.ResourceMapper;
import com.nataliya.model.ResourceType;
import com.nataliya.model.entity.Resource;
import com.nataliya.util.PathUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileSystemService {

    private final ResourceMetadataService resourceMetadataService;
    private final FileUploadService fileUploadService;
    private final DownloadService downloadService;
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

    public DownloadResourceDto download(Long userId, String resourcePath) {
        return downloadService.prepareDownload(userId, resourcePath);
    }

    public List<ResourceResponseDto> search(Long userId, String query) {
        List<Resource> searchResult = resourceMetadataService.getSearchResult(userId, query);
        return resourceMapper.resourceListToDtoList(searchResult);
    }

    @Transactional
    public void deleteResource(Long userId, String resourcePath) {

        String normalizedPath = PathUtil.normalizePath(resourcePath);
        resourceMetadataService.requireResourceExists(userId, normalizedPath);

        List<Resource> subtree = resourceMetadataService.getDirectorySubtree(userId, normalizedPath);

        List<UUID> filesIds = subtree.stream()
                .filter(resource -> resource.getResourceType() == ResourceType.FILE)
                .map(Resource::getId)
                .toList();

        minioService.deleteFiles(filesIds);
        resourceMetadataService.deleteResources(subtree);
    }

    @Transactional
    public ResourceResponseDto move(Long userId, String fromPath, String toPath) {

        String sourcePath = PathUtil.normalizePath(fromPath);
        String destinationPath = PathUtil.normalizePath(toPath);

        resourceMetadataService.requireResourceExists(userId, sourcePath);

        Resource result;

        if (resourceNameChanged(sourcePath, destinationPath)) {
            result = renameSubtree(userId, sourcePath, destinationPath);
        } else {
            result = moveSubtree(userId, sourcePath, destinationPath);
        }

        return resourceMapper.resourceToResourceDto(result);
    }

    private boolean resourceNameChanged(String sourcePath, String destinationPath) {

        String sourceName = PathUtil.extractResourceName(sourcePath, false);
        String newName = PathUtil.extractResourceName(destinationPath, false);

        return !sourceName.equals(newName);
    }

    private Resource renameSubtree(Long userId, String sourcePath, String destinationPath) {

        String newName = PathUtil.extractResourceName(destinationPath, false);
        Resource resource = resourceMetadataService.getResource(userId, sourcePath);

        List<Resource> subtree = resourceMetadataService.getDirectorySubtree(userId, sourcePath);
        subtree.sort(Comparator.comparingInt(r -> r.getPath().length()));

        for (Resource node : subtree) {
            String newPath = buildRenamedPath(node, sourcePath, destinationPath);
            node.setPath(newPath);
        }

        resource.setResourceName(newName);
        return resource;
    }

    private String buildRenamedPath(Resource resource, String sourcePath, String destinationPath) {

        String suffix = resource.getPath().substring(sourcePath.length());
        return destinationPath + suffix;
    }

    private Resource moveSubtree(Long userId, String sourceFullPath, String destinationPath) {

        String destinationParentPath = PathUtil.extractParentDirectoryPath(destinationPath);
        Resource destinationParent = resourceMetadataService.getResource(userId, destinationParentPath);

        List<Resource> subtree = resourceMetadataService.getDirectorySubtree(userId, sourceFullPath);
        subtree.sort(Comparator.comparingInt(r -> r.getPath().length()));

        int sourcePathLength = sourceFullPath.length();

        for (Resource node : subtree) {
            String newPath = destinationPath + node.getPath().substring(sourcePathLength);

            if (node.getResourceType() == ResourceType.FILE) {
                moveFile(userId, node, newPath, destinationPath, destinationParent);
            } else {
                moveDirectory(userId, node, newPath, destinationPath, destinationParent);
            }
        }
        return resourceMetadataService.getResource(userId, destinationPath);
    }

    private void moveFile(Long userId, Resource resource, String newPath, String destinationPath, Resource destinationParent) {

        resourceMetadataService.requireFileNotExists(userId, newPath);
        resource.setPath(newPath);

        if (newPath.equals(destinationPath)) {
            resource.setParent(destinationParent);
        }
    }

    private void moveDirectory(Long userId, Resource resource, String newPath, String destinationPath, Resource destinationParent) {

        if (resourceMetadataService.exists(userId, newPath)) {
            Resource newParent = resourceMetadataService.getResource(userId, newPath);

            for (Resource child : resource.getChildren()) {
                child.setParent(newParent);
            }
            resourceMetadataService.deleteResource(resource);

        } else {
            resource.setPath(newPath);

            if (newPath.equals(destinationPath)) {
                resource.setParent(destinationParent);
            }
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
