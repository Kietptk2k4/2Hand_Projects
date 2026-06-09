package com.twohands.commerce_service.infrastructure.ghn;

import org.springframework.util.StringUtils;

public final class GhnDistrictIdParser {

    private GhnDistrictIdParser() {
    }

    public static int parseRequired(String districtCode, String fieldLabel) {
        if (!StringUtils.hasText(districtCode)) {
            throw new IllegalArgumentException(fieldLabel + " is required");
        }
        try {
            int districtId = Integer.parseInt(districtCode.trim());
            if (districtId <= 0) {
                throw new IllegalArgumentException(fieldLabel + " must be a positive GHN district id");
            }
            return districtId;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldLabel + " must be a numeric GHN district id", ex);
        }
    }

    public static int parseOrZero(String districtCode) {
        if (!StringUtils.hasText(districtCode)) {
            return 0;
        }
        try {
            return Integer.parseInt(districtCode.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
