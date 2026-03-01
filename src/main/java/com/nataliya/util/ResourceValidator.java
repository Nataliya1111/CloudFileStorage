package com.nataliya.util;

import com.nataliya.exception.ResourceConflictException;
import com.nataliya.exception.ResourceNotFoundException;
import com.nataliya.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceValidator {

    private final ResourceRepository resourceRepository;

//    public void requireDirectoryDoesNotExist(Long userId, String directoryPath) {
//        if (resourceRepository.existsByUserIdAndPath(userId, directoryPath)) {
//            throw new ResourceConflictException(String
//                    .format("Directory '%s' of user with userId=%d already exists", directoryPath, userId));
//        }
//    }

    public void requireDirectoryExists(Long userId, String directoryPath) {
        if (!resourceRepository.existsByUserIdAndPath(userId, directoryPath)) {
            throw new ResourceNotFoundException(String
                    .format("Directory '%s' of user with userId=%d is not found", directoryPath, userId));
        }
    }
}
