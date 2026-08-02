package com.twohands.commerce_service.unit.domain.catalog;

import com.twohands.commerce_service.domain.catalog.FlashSaleSlot;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class FlashSaleSlotTest {

    @Test
    void currentShouldReturnThreeHourWindowInHoChiMinhTimezone() {
        Instant now = ZonedDateTime.of(2026, 8, 2, 16, 30, 0, 0, ZoneId.of("Asia/Ho_Chi_Minh"))
                .toInstant();

        FlashSaleSlot slot = FlashSaleSlot.current(now);

        ZonedDateTime start = slot.start().atZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        ZonedDateTime end = slot.end().atZone(ZoneId.of("Asia/Ho_Chi_Minh"));

        assertThat(start.getHour()).isEqualTo(15);
        assertThat(end.getHour()).isEqualTo(18);
        assertThat(end.toInstant()).isAfter(start.toInstant());
    }
}
