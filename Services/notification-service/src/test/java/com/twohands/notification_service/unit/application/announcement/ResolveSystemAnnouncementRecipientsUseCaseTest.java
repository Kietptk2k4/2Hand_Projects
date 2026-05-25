package com.twohands.notification_service.unit.application.announcement;

import com.twohands.notification_service.application.announcement.ResolveSystemAnnouncementRecipientsUseCase;
import com.twohands.notification_service.domain.admin.SystemAnnouncementFanOutContext;
import com.twohands.notification_service.domain.announcement.SystemAnnouncementAudienceUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResolveSystemAnnouncementRecipientsUseCaseTest {

    @Mock
    private SystemAnnouncementAudienceUserProvider audienceUserProvider;

    private ResolveSystemAnnouncementRecipientsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ResolveSystemAnnouncementRecipientsUseCase(audienceUserProvider, 500);
    }

    @Test
    void execute_returnsExplicitRecipients() {
        UUID userOne = UUID.randomUUID();
        UUID userTwo = UUID.randomUUID();
        var context = new SystemAnnouncementFanOutContext(
                "ann-1",
                "Title",
                "Content",
                "INFO",
                false,
                true,
                List.of(userOne, userTwo),
                null,
                "SYSTEM_ANNOUNCEMENT",
                "ann-1"
        );

        List<UUID> recipients = useCase.execute(context);

        assertEquals(List.of(userOne, userTwo), recipients);
    }

    @Test
    void execute_throwsWhenNoRecipientsOrAudience() {
        var context = new SystemAnnouncementFanOutContext(
                "ann-1",
                "Title",
                "Content",
                "INFO",
                false,
                true,
                List.of(),
                null,
                "SYSTEM_ANNOUNCEMENT",
                "ann-1"
        );

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(context));
    }

    @Test
    void execute_throwsWhenAudienceProviderReturnsEmpty() {
        when(audienceUserProvider.supports("ALL_USERS")).thenReturn(true);
        when(audienceUserProvider.fetchPage("ALL_USERS", 0, 500)).thenReturn(List.of());

        var context = new SystemAnnouncementFanOutContext(
                "ann-1",
                "Title",
                "Content",
                "INFO",
                false,
                true,
                List.of(),
                "ALL_USERS",
                "SYSTEM_ANNOUNCEMENT",
                "ann-1"
        );

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(context));
    }
}
