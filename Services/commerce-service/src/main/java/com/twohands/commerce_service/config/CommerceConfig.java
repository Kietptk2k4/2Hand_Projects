package com.twohands.commerce_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        CommerceIntegrationProperties.class,
        CommerceObjectStorageProperties.class
})
public class CommerceConfig {
}
