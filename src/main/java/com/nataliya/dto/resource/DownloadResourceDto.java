package com.nataliya.dto.resource;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public record DownloadResourceDto(
        String filename,
        StreamingResponseBody body
) {
}
