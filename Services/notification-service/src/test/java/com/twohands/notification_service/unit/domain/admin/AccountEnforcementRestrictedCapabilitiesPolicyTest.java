package com.twohands.notification_service.unit.domain.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.admin.AccountEnforcementRestrictedCapabilitiesPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AccountEnforcementRestrictedCapabilitiesPolicyTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void resolveSummary_mapsKnownCapabilitiesToUserFacingLabels() throws Exception {
        String summary = AccountEnforcementRestrictedCapabilitiesPolicy.resolveSummary(
                objectMapper.readTree("""
                        ["POST_CREATE","COMMENT_CREATE"]
                        """)
        );

        assertEquals("Creating posts, Commenting", summary);
    }

    @Test
    void resolveSummary_ignoresUnsafeCapabilityText() throws Exception {
        String summary = AccountEnforcementRestrictedCapabilitiesPolicy.resolveSummary(
                objectMapper.readTree("""
                        ["internal_investigation_access"]
                        """)
        );

        assertNull(summary);
    }

    @Test
    void resolveSummary_supportsCommaSeparatedString() throws Exception {
        String summary = AccountEnforcementRestrictedCapabilitiesPolicy.resolveSummary(
                objectMapper.readTree("\"PRODUCT_CREATE, REVIEW_CREATE\"")
        );

        assertEquals("Listing products, Writing reviews", summary);
    }
}
