package com.twohands.auth_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AuthObjectStorageProperties.class)
public class AuthObjectStorageConfig {
}
