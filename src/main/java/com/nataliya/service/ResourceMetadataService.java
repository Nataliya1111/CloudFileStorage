package com.nataliya.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.nataliya.exception.FileAlreadyExistsException;
import com.nataliya.exception.ResourceNotFoundException;
import com.nataliya.model.ResourceType;
import com.nataliya.model.entity.Resource;
import com.nataliya.model.entity.User;
import com.nataliya.repository.ResourceRepository;
import com.nataliya.repository.UserRepository;
import com.nataliya.util.PathUtil;
import lombok.RequiredArgsConstructor;
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

    public UUID createFileMetadata(Long userId, String directoryPath, String fullFileName, long filesize) throws FileAlreadyExistsException {

        User user = userRepository.getReferenceById(userId);

        Resource targetDirectory = findTargetDirectory(userId, directoryPath);

        String relativePathToFile = PathUtil.extractParentDirectoryPath(fullFileName);
        String fileName = PathUtil.extractResourceName(fullFileName, false);

        Resource deepestDirectory = createDirectories(user, relativePathToFile, targetDirectory);

        return createFile(user, deepestDirectory, fileName, filesize);
    }

    public List<Resource> getDirectoryContentsList(Long userId, String directoryPath) {
        return resourceRepository.findAllByUserIdAndParentPath(userId, directoryPath);
    }

    private Resource findTargetDirectory(Long userId, String directoryPath) {

        return resourceRepository.findByUserIdAndPath(userId, directoryPath)
                .orElseThrow(() -> new ResourceNotFoundException(String
                        .format("Directory '%s' of user with userId=%d is not found", directoryPath, userId)));
    }

    private Resource createDirectories(User user, String relativePathToFile, Resource targetDirectory) {
        if (relativePathToFile == null || relativePathToFile.isBlank()) {
            return targetDirectory;
        }

        String[] directoryNames = relativePathToFile.split("/");

        Resource parent = targetDirectory;
        String path = "";

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

    private UUID createFile(User user, Resource parent, String fileName, long size) throws FileAlreadyExistsException {
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

        boolean fileExists = resourceRepository
                .existsByUserIdAndParentIdAndResourceName(
                        user.getId(),
                        parent != null ? parent.getId() : null,
                        fileName
                );
        if (fileExists) {
            throw new FileAlreadyExistsException("File already Exists in resources table", filePath);
        }
        resourceRepository.save(file);
        return fileId;
    }

}
