package com.nataliya.service;

import com.nataliya.dto.resource.ResourceResponseDto;
import com.nataliya.exception.FileAlreadyExistsException;
import com.nataliya.exception.PartialUploadException;
import com.nataliya.exception.ResourceConflictException;
import com.nataliya.mapper.ResourceMapper;
import com.nataliya.model.entity.Resource;
import com.nataliya.util.PathUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileSystemService {

    private final ResourceMetadataService resourceMetadataService;
    private final FileUploadService fileUploadService;
    private final ResourceMapper resourceMapper;

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
