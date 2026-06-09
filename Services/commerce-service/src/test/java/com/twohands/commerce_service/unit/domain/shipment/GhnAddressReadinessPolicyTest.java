package com.twohands.commerce_service.unit.domain.shipment;

import com.twohands.commerce_service.domain.shipment.GhnAddressReadinessPolicy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GhnAddressReadinessPolicyTest {

    @Test
    void acceptsNumericDistrictAndWard() {
        assertThat(GhnAddressReadinessPolicy.isReadyForGhn("1444", "20308")).isTrue();
        assertThat(GhnAddressReadinessPolicy.validateDistrictAndWard("1444", "20308")).isEmpty();
    }

    @Test
    void rejectsMissingWard() {
        assertThat(GhnAddressReadinessPolicy.isReadyForGhn("1444", "")).isFalse();
        assertThat(GhnAddressReadinessPolicy.validateDistrictAndWard("1444", null))
                .anyMatch(issue -> issue.contains("ward_code"));
    }

    @Test
    void rejectsNonNumericDistrict() {
        assertThat(GhnAddressReadinessPolicy.isReadyForGhn("Quan 10", "20308")).isFalse();
        assertThat(GhnAddressReadinessPolicy.validateDistrictAndWard("Quan 10", "20308"))
                .anyMatch(issue -> issue.contains("numeric"));
    }
}
