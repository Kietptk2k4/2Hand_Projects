package com.twohands.notification_service.domain.announcement;

import java.util.List;
import java.util.UUID;

/**
 * Resolves user ids for {@code target_audience} when explicit {@code recipient_user_ids} are absent.
 * MVP ships without Auth paging; implementations may return empty until wired.
 */
public interface SystemAnnouncementAudienceUserProvider {

    boolean supports(String targetAudience);

    List<UUID> fetchPage(String targetAudience, int offset, int limit);
}
