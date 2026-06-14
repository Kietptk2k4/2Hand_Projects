package com.twohands.commerce_service.application.catalog.admin.setbrandactive;

import java.util.UUID;

public record SetBrandActiveCommand(UUID brandId, boolean active) {
}
