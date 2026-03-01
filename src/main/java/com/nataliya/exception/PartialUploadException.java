package com.nataliya.exception;

public class PartialUploadException extends RuntimeException {

    public PartialUploadException(String message) {
        super(message);
    }
}
