package com.nataliya.exception;

public class FilesNotUploadedException extends RuntimeException {

    public FilesNotUploadedException(String message) {
        super(message);
    }
}
