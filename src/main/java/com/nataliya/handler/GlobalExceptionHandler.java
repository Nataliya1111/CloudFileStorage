package com.nataliya.handler;

import com.nataliya.dto.response.error.ErrorResponseDto;
import com.nataliya.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileCountLimitExceededException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Value("${server.tomcat.max-part-count}")
    private int maxPartCount;

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleException(UserAlreadyExistsException ex) {

        log.info("Attempt to create duplicate user");

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        BindingResult bindingResult = ex.getBindingResult();

        List<ObjectError> errors = new ArrayList<>();
        errors.addAll(bindingResult.getGlobalErrors());
        errors.addAll(bindingResult.getFieldErrors());

        String message = errors.stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.joining("; "));

        log.info("Validation failed for {}: {}",
                request.getDescription(false),
                message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) //400
                .body(new ErrorResponseDto(message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleException(ConstraintViolationException ex, HttpServletRequest request) {

        log.info("Validation failed at [{} {}]: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage(),
                ex);

        String message = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Validation error");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) //400
                .body(new ErrorResponseDto(message));
    }

    @ExceptionHandler(MinioStorageException.class)
    public ResponseEntity<ErrorResponseDto> handleException(MinioStorageException ex) {

        log.error("Exception at MinioService: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleException(ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("Exception at [{} {}]: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage(),
                ex);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND) //404
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ErrorResponseDto> handleException(ResourceConflictException ex, HttpServletRequest request) {

        log.info("Exception at [{} {}]: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage(),
                ex);

        return ResponseEntity
                .status(HttpStatus.CONFLICT) //409
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(PartialUploadException.class)
    public ResponseEntity<ErrorResponseDto> handleException(PartialUploadException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT) //409
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(StorageLimitExceededException.class)
    public ResponseEntity<ErrorResponseDto> handleException(StorageLimitExceededException ex) {

        log.warn("Attempt to upload files out of user or server memory limits", ex);

        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE) //413
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponseDto> handleException(MultipartException ex) {

        Throwable root = NestedExceptionUtils.getMostSpecificCause(ex);

        if (root instanceof FileCountLimitExceededException) {
            String message = "Too many files uploaded. Maximum allowed is " + maxPartCount;

            log.warn(message, ex);

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST) //400
                    .body(new ErrorResponseDto(message));
        }

        throw ex;
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            @NotNull MaxUploadSizeExceededException ex,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode status,
            @NotNull WebRequest request) {

        log.warn("Attempt to upload too large file", ex);

        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE) //413
                .body(new ErrorResponseDto("File too large"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception at [{} {}]: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage(),
                ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto("Unexpected server error. Please try again later."));
    }

}
