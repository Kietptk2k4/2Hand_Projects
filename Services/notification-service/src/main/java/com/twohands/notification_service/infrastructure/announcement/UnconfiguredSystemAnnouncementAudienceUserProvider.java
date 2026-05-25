package com.twohands.notification_service.infrastructure.announcement;

import com.twohands.notification_service.domain.announcement.SystemAnnouncementAudienceUserProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class UnconfiguredSystemAnnouncementAudienceUserProvider implements SystemAnnouncementAudienceUserProvider {

    @Override
    public boolean supports(String targetAudience) {
        return targetAudience != null && !targetAudience.isBlank();
    }

    @Override
    public List<UUID> fetchPage(String targetAudience, int offset, int limit) {
        return List.of();
    }
}
