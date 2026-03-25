package com.nataliya.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.nataliya.exception.FileAlreadyExistsException;
import com.nataliya.exception.ResourceConflictException;
import com.nataliya.exception.ResourceNotFoundException;
import com.nataliya.model.ResourceType;
import com.nataliya.model.entity.Resource;
import com.nataliya.model.entity.User;
import com.nataliya.repository.ResourceRepository;
import com.nataliya.repository.UserRepository;
import com.nataliya.util.PathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceMetadataService {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    public void createRootDirectoryMetadata(Long userId) {

        UUID rootDirectoryId = UuidCreator.getTimeOrderedEpoch();
        User user = userRepository.getReferenceById(userId);

        Resource rootDirectory = Resource.builder()
                .id(rootDirectoryId)
                .user(user)
                .resourceName("")
                .parent(null)
                .path("/")
                .resourceType(ResourceType.DIRECTORY)
                .build();

        resourceRepository.save(rootDirectory);
    }

    public Resource getResource(Long userId, String resourcePath) {

        ResourceType type = resourcePath.endsWith("/") ? ResourceType.DIRECTORY : ResourceType.FILE;

        return resourceRepository.findByUserIdAndPath(userId, resourcePath)
                .orElseThrow(() -> new ResourceNotFoundException(String
                        .format("%s '%s' of user with id '%d' is not found", type, resourcePath, userId)));
    }

    public Resource createFileMetadata(Long userId, String directoryPath, String fullFileName, long filesize) throws FileAlreadyExistsException {

        User user = userRepository.getReferenceById(userId);

        Resource targetDirectory = getResource(userId, directoryPath);

        String relativePathToFile = PathUtil.extractParentDirectoryPath(fullFileName);
        String fileName = PathUtil.extractResourceName(fullFileName, false);

        Resource deepestDirectory = createDirectoryHierarchyMetadata(user, relativePathToFile, targetDirectory);

        return createFileMetadataEntry(user, deepestDirectory, fileName, filesize);
    }

    public Resource createEmptyDirectory(Long userId, String directoryPath) {

        Resource directory = buildResource(userId, directoryPath);

        try {
            resourceRepository.saveAndFlush(directory);
            return directory;
        } catch (DataIntegrityViolationException e) {
            throw new ResourceConflictException(String
                    .format("Directory '%s' of user with id '%d' already exists", directoryPath, userId));
        }
    }

    public List<Resource> getDirectoryContentsList(Long userId, String directoryPath) {

        UUID parentId = resourceRepository.findByUserIdAndPath(userId, directoryPath)
                .orElseThrow(() -> new ResourceNotFoundException(String
                        .format("Directory '%s' of user with id '%d' is not found", directoryPath, userId)))
                .getId();

        return resourceRepository.findAllByUserIdAndParentId(userId, parentId);
    }

    public List<Resource> getDirectorySubtree(Long userId, String directoryPath) {
        return resourceRepository.findByUserIdAndPathStartingWith(userId, directoryPath);
    }

    public void deleteResource(Resource resource) {
        resourceRepository.delete(resource);
    }

    public void deleteResources(List<Resource> resources) {
        resourceRepository.deleteAllInBatch(resources);
    }

    public boolean exists(Long userId, String resourcePath) {
        return resourceRepository.existsByUserIdAndPath(userId, resourcePath);
    }

    public void requireResourceExists(Long userId, String resourcePath) {
        if (!resourceRepository.existsByUserIdAndPath(userId, resourcePath)) {
            throw new ResourceNotFoundException(String
                    .format("Resource '%s' of user with id '%s' is not found", resourcePath, userId));
        }
    }

    public void requireFileNotExists(Long userId, String resourcePath) {
        if (resourceRepository.existsByUserIdAndPath(userId, resourcePath)) {
            String message = String
                    .format("File '%s' of user with userId=%d already exists", resourcePath, userId);
            throw new FileAlreadyExistsException(message, resourcePath);
        }
    }

    public long getUserStorageUsage(Long userId) {
        return resourceRepository.getUserStorageUsage(userId);
    }

    public long getTotalStorageUsage() {
        return resourceRepository.getTotalStorageUsage();
    }

    public List<Resource> getSearchResult(Long userId, String query) {
        return resourceRepository.searchByResourceName(userId, query);
    }

    private Resource buildResource(Long userId, String directoryPath) {

        User user = userRepository.getReferenceById(userId);
        String directoryName = PathUtil.extractResourceName(directoryPath, false);
        String relativePathToDirectory = PathUtil.extractParentDirectoryPath(directoryPath);
        Resource parent = getResource(userId, relativePathToDirectory);

        return Resource.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .user(user)
                .resourceName(directoryName)
                .parent(parent)
                .path(directoryPath)
                .resourceType(ResourceType.DIRECTORY)
                .build();
    }

    private Resource createDirectoryHierarchyMetadata(User user, String relativePathToFile, Resource targetDirectory) {
        if (relativePathToFile == null || relativePathToFile.isBlank()) {
            return targetDirectory;
        }

        String[] directoryNames = relativePathToFile.split("/");

        Resource parent = targetDirectory;
        String path = targetDirectory.getPath().equals("/") ? "" : targetDirectory.getPath();

        for (String directoryName : directoryNames) {

            if (directoryName.isBlank()) {
                continue;
            }
            path += directoryName + "/";

            Resource directory = Resource.builder()
                    .id(UuidCreator.getTimeOrderedEpoch())
                    .user(user)
                    .resourceName(directoryName)
                    .parent(parent)
                    .path(path)
                    .resourceType(ResourceType.DIRECTORY)
                    .build();
            parent = getOrCrateDirectory(directory);
        }
        return parent;
    }

    private Resource getOrCrateDirectory(Resource directory) {

        Optional<Resource> existing = resourceRepository
                .findByUserIdAndParentIdAndResourceName(
                        directory.getUser().getId(),
                        directory.getParent() != null ? directory.getParent().getId() : null,
                        directory.getResourceName()
                );

        if (existing.isPresent()) {
            Resource resource = existing.get();

            if (resource.getResourceType() != ResourceType.DIRECTORY) {
                throw new ResourceConflictException(
                        String.format(
                                "'%s' already exists and is a file",
                                resource.getPath()
                        )
                );
            }
            return resource;
        }
        return resourceRepository.save(directory);
    }

    private Resource createFileMetadataEntry(User user, Resource parent, String fileName, long size)
            throws FileAlreadyExistsException {

        if (parent == null) {
            throw new ResourceNotFoundException(
                    String.format("Parent directory for file '%s' not found", fileName)
            );
        }

        UUID fileId = UuidCreator.getTimeOrderedEpoch();

        String filePath = parent.getPath().equals("/") ? fileName : parent.getPath() + fileName;

        Resource file = Resource.builder()
                .id(fileId)
                .user(user)
                .resourceName(fileName)
                .parent(parent)
                .path(filePath)
                .resourceType(ResourceType.FILE)
                .size(size)
                .build();
        try {
            resourceRepository.saveAndFlush(file);
            return file;
        } catch (DataIntegrityViolationException e) {
            String message = String
                    .format("File '%s' in directory '%s' of user with userId=%d already exists", fileName, filePath, user.getId());
            throw new FileAlreadyExistsException(message, filePath);
        }
    }
}
