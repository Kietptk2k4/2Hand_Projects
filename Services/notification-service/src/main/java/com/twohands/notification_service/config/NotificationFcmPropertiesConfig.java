package com.twohands.notification_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NotificationFcmProperties.class)
public class NotificationFcmPropertiesConfig {
}
