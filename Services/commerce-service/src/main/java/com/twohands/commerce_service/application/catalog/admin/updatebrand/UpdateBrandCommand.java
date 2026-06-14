package com.twohands.commerce_service.application.catalog.admin.updatebrand;

import java.util.UUID;

public record UpdateBrandCommand(UUID brandId, String name, String slug) {
}
