package com.twohands.auth_service.unit.application.outbox;

import com.twohands.auth_service.application.outbox.OutboxPublishErrorSanitizer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxPublishErrorSanitizerTest {

    @Test
    void shouldRedactSensitiveValues() {
        String sanitized = OutboxPublishErrorSanitizer.sanitize("publish failed: password=secret123 token=abc");

        assertThat(sanitized).doesNotContain("secret123");
        assertThat(sanitized).doesNotContain("abc");
        assertThat(sanitized).contains("password=");
        assertThat(sanitized).contains("***");
    }
}
