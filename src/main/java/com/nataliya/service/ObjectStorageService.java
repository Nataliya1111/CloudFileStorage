package com.nataliya.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface ObjectStorageService {

    void uploadFile(UUID objectKey, MultipartFile file);

    InputStream getFileStream(UUID objectKey);

    void deleteFiles(List<UUID> objectKeys);

}
