package com.nataliya.service;

import com.nataliya.dto.resource.ResourceResponseDto;
import com.nataliya.exception.FileAlreadyExistsException;
import com.nataliya.exception.FilesNotUploadedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileSystemService {

    private final ResourceMetadataService resourceMetadataService;
    private final MinioService minioService;
    private final FileUploadService fileUploadService;

    public List<ResourceResponseDto> uploadFiles(Long userId, String relativeDirectoryPath, List<MultipartFile> files) {

        List<ResourceResponseDto> uploadedResourcesDtos = new ArrayList<>();
        List<String> failedFilePaths = new ArrayList<>();

        for (MultipartFile file : files) {

            try {
                ResourceResponseDto responseDto = fileUploadService
                        .uploadSingleFile(userId, file, relativeDirectoryPath);
                uploadedResourcesDtos.add(responseDto);
            } catch (FileAlreadyExistsException e) {
                log.info("Attempt to save duplicate file {}", e.getFilePath());
                failedFilePaths.add(e.getFilePath());
            }
        }

        if (!failedFilePaths.isEmpty()) {
            throw buildFilesNotUploadedException(files.size(), failedFilePaths);
        }

        return uploadedResourcesDtos;
    }

    private FilesNotUploadedException buildFilesNotUploadedException(
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

        return new FilesNotUploadedException(message);
    }
}
