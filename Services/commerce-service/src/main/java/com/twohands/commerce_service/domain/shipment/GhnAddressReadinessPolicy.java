package com.twohands.commerce_service.domain.shipment;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class GhnAddressReadinessPolicy {

    private GhnAddressReadinessPolicy() {
    }

    public static List<String> validateDistrictAndWard(String districtCode, String wardCode) {
        List<String> issues = new ArrayList<>();
        if (!StringUtils.hasText(wardCode)) {
            issues.add("ward_code is required for GHN (GHN ward code, e.g. 20308)");
        }
        if (!StringUtils.hasText(districtCode)) {
            issues.add("district_code is required for GHN (numeric district id)");
            return issues;
        }
        try {
            int districtId = Integer.parseInt(districtCode.trim());
            if (districtId <= 0) {
                issues.add("district_code must be a positive GHN district id");
            }
        } catch (NumberFormatException ex) {
            issues.add("district_code must be numeric GHN district id, not a province or district name");
        }
        return List.copyOf(issues);
    }

    public static boolean isReadyForGhn(String districtCode, String wardCode) {
        return validateDistrictAndWard(districtCode, wardCode).isEmpty();
    }
}
