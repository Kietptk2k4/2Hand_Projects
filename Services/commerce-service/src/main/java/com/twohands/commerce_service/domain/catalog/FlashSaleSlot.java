package com.twohands.commerce_service.domain.catalog;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public record FlashSaleSlot(Instant start, Instant end) {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int SLOT_HOURS = 3;

    public static FlashSaleSlot current(Instant now) {
        ZonedDateTime zoned = now.atZone(ZONE);
        int slotHour = (zoned.getHour() / SLOT_HOURS) * SLOT_HOURS;
        ZonedDateTime slotStart = zoned.withHour(slotHour).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime slotEnd = slotStart.plusHours(SLOT_HOURS);
        return new FlashSaleSlot(slotStart.toInstant(), slotEnd.toInstant());
    }
}
