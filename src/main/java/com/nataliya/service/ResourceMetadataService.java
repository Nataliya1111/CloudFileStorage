package com.nataliya.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.nataliya.model.ResourceType;
import com.nataliya.model.entity.Resource;
import com.nataliya.model.entity.User;
import com.nataliya.repository.ResourceRepository;
import com.nataliya.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceMetadataService {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    public UUID createMetadata(Long userId, String directoryPath, String fileName, long filesize) {

        User user = userRepository.getReferenceById(userId);

        Resource parent = createDirectories(user, directoryPath);

        return createFile(user, parent, fileName, filesize);
    }

    private Resource createDirectories(User user, String directoryPath) {
        if (directoryPath == null || directoryPath.isBlank()) {
            return null;
        }

        String[] directoryNames = directoryPath.split("/");

        Resource parent = null;
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
            parent = getOrSaveResource(directory);
        }
        return parent;
    }

    private UUID createFile(User user, Resource parent, String fileName, long size) {
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

        getOrSaveResource(file);
        return fileId;
    }

    private Resource getOrSaveResource(Resource resource) {
        try {
            return resourceRepository.save(resource);
        } catch (DataIntegrityViolationException e) {
            return resourceRepository.findByUserIdAndParentIdAndResourceName(
                            resource.getUser().getId(),
                            resource.getParent() != null ? resource.getParent().getId() : null,
                            resource.getResourceName()
                    )
                    .orElseThrow(() -> e);
        }
    }
}
