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

    public Resource createFileMetadata(Long userId, String directoryPath, String fullFileName, long filesize) throws FileAlreadyExistsException {

        User user = userRepository.getReferenceById(userId);

        Resource targetDirectory = findDirectory(userId, user.getUsername(), directoryPath);

        String relativePathToFile = PathUtil.extractParentDirectoryPath(fullFileName);
        String fileName = PathUtil.extractResourceName(fullFileName, false);

        Resource deepestDirectory = createDirectoryHierarchyMetadata(user, relativePathToFile, targetDirectory);

        return createFileMetadataEntry(user, deepestDirectory, fileName, filesize);
    }

    public Resource createEmptyDirectory(Long userId, String directoryPath) {

        User user = userRepository.getReferenceById(userId);
        String directoryName = PathUtil.extractResourceName(directoryPath, false);
        String relativePathToDirectory = PathUtil.extractParentDirectoryPath(directoryPath);
        Resource parent = findDirectory(userId, user.getUsername(), relativePathToDirectory);

        Resource directory = Resource.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .user(user)
                .resourceName(directoryName)
                .parent(parent)
                .path(directoryPath)
                .resourceType(ResourceType.DIRECTORY)
                .build();

        try {
            resourceRepository.saveAndFlush(directory);
            return directory;
        } catch (DataIntegrityViolationException e) {
            throw new ResourceConflictException(String
                    .format("Directory '%s' of user %s already exists", directoryPath, user.getUsername()));
        }
    }

    public List<Resource> getDirectoryContentsList(Long userId, String directoryPath) {

        User user = userRepository.getReferenceById(userId);
        requireDirectoryExists(userId, user.getUsername(), directoryPath);
        return resourceRepository.findAllByUserIdAndParentPath(userId, directoryPath);
    }

    private Resource findDirectory(Long userId, String username, String directoryPath) {

        return resourceRepository.findByUserIdAndPath(userId, directoryPath)
                .orElseThrow(() -> new ResourceNotFoundException(String
                        .format("Directory '%s' of user %s is not found", directoryPath, username)));
    }

    private void requireDirectoryExists(Long userId, String username, String directoryPath) {
        if (!resourceRepository.existsByUserIdAndPath(userId, directoryPath)) {
            throw new ResourceNotFoundException(String
                    .format("Directory '%s' of user %s is not found", directoryPath, username));
        }
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
            parent = getOrSaveDirectory(directory);
        }
        return parent;
    }

    private Resource getOrSaveDirectory(Resource resource) {
        return resourceRepository
                .findByUserIdAndParentIdAndResourceName(
                        resource.getUser().getId(),
                        resource.getParent() != null ? resource.getParent().getId() : null,
                        resource.getResourceName()
                )
                .orElseGet(() -> resourceRepository.save(resource));
    }

    private Resource createFileMetadataEntry(User user, Resource parent, String fileName, long size) throws FileAlreadyExistsException {
        UUID fileId = UuidCreator.getTimeOrderedEpoch();
        String filePath = parent != null ? parent.getPath() + fileName : fileName;

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
