package com.twohands.notification_service.application.announcement;

import com.twohands.notification_service.domain.admin.SystemAnnouncementFanOutContext;
import com.twohands.notification_service.domain.announcement.SystemAnnouncementAudienceUserProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ResolveSystemAnnouncementRecipientsUseCase {

    private final SystemAnnouncementAudienceUserProvider audienceUserProvider;
    private final int audiencePageSize;

    public ResolveSystemAnnouncementRecipientsUseCase(
            SystemAnnouncementAudienceUserProvider audienceUserProvider,
            @Value("${notification.system-announcement.audience-page-size:500}") int audiencePageSize
    ) {
        this.audienceUserProvider = audienceUserProvider;
        this.audiencePageSize = Math.max(audiencePageSize, 1);
    }

    public List<UUID> execute(SystemAnnouncementFanOutContext context) {
        Set<UUID> recipients = new LinkedHashSet<>(context.explicitRecipientUserIds());
        if (!recipients.isEmpty()) {
            return List.copyOf(recipients);
        }

        String targetAudience = context.targetAudience();
        if (targetAudience == null || targetAudience.isBlank()) {
            throw new IllegalArgumentException(
                    "recipient_user_ids or target_audience is required for SYSTEM_ANNOUNCEMENT_SENT fan-out."
            );
        }

        if (!audienceUserProvider.supports(targetAudience)) {
            throw new IllegalArgumentException("Unsupported target_audience: " + targetAudience);
        }

        int offset = 0;
        while (true) {
            List<UUID> page = audienceUserProvider.fetchPage(targetAudience, offset, audiencePageSize);
            if (page.isEmpty()) {
                break;
            }
            recipients.addAll(page);
            if (page.size() < audiencePageSize) {
                break;
            }
            offset += audiencePageSize;
        }

        if (recipients.isEmpty()) {
            throw new IllegalArgumentException(
                    "No recipients resolved for target_audience; audience provider is not configured."
            );
        }

        return new ArrayList<>(recipients);
    }
}
