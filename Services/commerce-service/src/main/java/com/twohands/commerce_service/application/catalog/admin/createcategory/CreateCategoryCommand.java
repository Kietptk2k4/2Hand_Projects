package com.twohands.commerce_service.application.catalog.admin.createcategory;

import java.util.UUID;

public record CreateCategoryCommand(String name, String slug, UUID parentId) {
}
