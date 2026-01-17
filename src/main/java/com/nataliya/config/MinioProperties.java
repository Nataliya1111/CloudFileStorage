package com.nataliya.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationProperties(prefix = "minio")
@ConfigurationPropertiesScan
@Data
public class MinioProperties {
    private String url;
    private String accessKey;
    private String secretKey;
    private String bucketName;
}
