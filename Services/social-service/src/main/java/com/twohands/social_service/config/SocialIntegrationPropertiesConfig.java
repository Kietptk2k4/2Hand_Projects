package com.twohands.social_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SocialKafkaConsumerProperties.class)
public class SocialIntegrationPropertiesConfig {
}
