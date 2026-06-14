package com.twohands.commerce_service.application.catalog.admin.setcategoryactive;

import java.util.UUID;

public record SetCategoryActiveCommand(UUID categoryId, boolean active) {
}
