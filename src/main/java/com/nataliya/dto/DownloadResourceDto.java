package com.nataliya.dto;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public record DownloadResourceDto(
        String filename,
        StreamingResponseBody body
) {
}
