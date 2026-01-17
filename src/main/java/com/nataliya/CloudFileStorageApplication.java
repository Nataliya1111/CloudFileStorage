package com.nataliya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.nataliya.config")
public class CloudFileStorageApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudFileStorageApplication.class, args);
    }

}
