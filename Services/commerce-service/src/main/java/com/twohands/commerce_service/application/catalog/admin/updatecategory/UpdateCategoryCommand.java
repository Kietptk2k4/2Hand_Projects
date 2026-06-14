package com.twohands.commerce_service.application.catalog.admin.updatecategory;

import java.util.UUID;

public record UpdateCategoryCommand(UUID categoryId, String name, String slug, UUID parentId) {
}
