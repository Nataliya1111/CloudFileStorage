package com.nataliya.exception;

import lombok.Getter;

@Getter
public class FileAlreadyExistsException extends Exception {

    private final String filePath;

    public FileAlreadyExistsException(String message, String filePath) {
        super(message);
        this.filePath = filePath;
    }
}