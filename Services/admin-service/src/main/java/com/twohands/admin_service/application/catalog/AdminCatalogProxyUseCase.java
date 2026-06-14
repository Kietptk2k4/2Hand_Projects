package com.twohands.admin_service.application.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommerceCatalogGateway;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class AdminCatalogProxyUseCase {

    private final AdminAuthorizationService adminAuthorizationService;
    private final CommerceCatalogGateway commerceCatalogGateway;
    private final AdminActionAuditLogger adminActionAuditLogger;

    public AdminCatalogProxyUseCase(
            AdminAuthorizationService adminAuthorizationService,
            CommerceCatalogGateway commerceCatalogGateway,
            AdminActionAuditLogger adminActionAuditLogger
    ) {
        this.adminAuthorizationService = adminAuthorizationService;
        this.commerceCatalogGateway = commerceCatalogGateway;
        this.adminActionAuditLogger = adminActionAuditLogger;
    }

    @Transactional(readOnly = true)
    public JsonNode listCategories(Boolean isActive, String query, String bearerToken) {
        requireRead();
        return requireGateway().listCategories(isActive, query, bearerToken);
    }

    @Transactional
    public JsonNode createCategory(Map<String, Object> body, String bearerToken) {
        UUID adminId = requireWrite();
        JsonNode data = requireGateway().createCategory(body, bearerToken);
        logSuccess(adminId, AdminActionType.SYSTEM_CONFIG_UPDATE, AdminActionTargetType.CATEGORY, text(data, "id"));
        return data;
    }

    @Transactional
    public JsonNode updateCategory(UUID categoryId, Map<String, Object> body, String bearerToken) {
        UUID adminId = requireWrite();
        JsonNode data = requireGateway().updateCategory(categoryId, body, bearerToken);
        logSuccess(adminId, AdminActionType.SYSTEM_CONFIG_UPDATE, AdminActionTargetType.CATEGORY, categoryId.toString());
        return data;
    }

    @Transactional
    public JsonNode setCategoryActive(UUID categoryId, boolean active, String bearerToken) {
        UUID adminId = requireWrite();
        JsonNode data = requireGateway().setCategoryActive(categoryId, active, bearerToken);
        logSuccess(adminId, AdminActionType.SYSTEM_CONFIG_UPDATE, AdminActionTargetType.CATEGORY, categoryId.toString());
        return data;
    }

    @Transactional(readOnly = true)
    public JsonNode listBrands(Boolean isActive, String query, Integer page, Integer limit, String bearerToken) {
        requireRead();
        return requireGateway().listBrands(isActive, query, page, limit, bearerToken);
    }

    @Transactional
    public JsonNode createBrand(Map<String, Object> body, String bearerToken) {
        UUID adminId = requireWrite();
        JsonNode data = requireGateway().createBrand(body, bearerToken);
        logSuccess(adminId, AdminActionType.SYSTEM_CONFIG_UPDATE, AdminActionTargetType.BRAND, text(data, "id"));
        return data;
    }

    @Transactional
    public JsonNode updateBrand(UUID brandId, Map<String, Object> body, String bearerToken) {
        UUID adminId = requireWrite();
        JsonNode data = requireGateway().updateBrand(brandId, body, bearerToken);
        logSuccess(adminId, AdminActionType.SYSTEM_CONFIG_UPDATE, AdminActionTargetType.BRAND, brandId.toString());
        return data;
    }

    @Transactional
    public JsonNode setBrandActive(UUID brandId, boolean active, String bearerToken) {
        UUID adminId = requireWrite();
        JsonNode data = requireGateway().setBrandActive(brandId, active, bearerToken);
        logSuccess(adminId, AdminActionType.SYSTEM_CONFIG_UPDATE, AdminActionTargetType.BRAND, brandId.toString());
        return data;
    }

    private void requireRead() {
        adminAuthorizationService.requireCurrentAdminId();
        adminAuthorizationService.requirePermission(AdminPermission.CATALOG_READ);
    }

    private UUID requireWrite() {
        UUID adminId = adminAuthorizationService.requireCurrentAdminId();
        adminAuthorizationService.requirePermission(AdminPermission.CATALOG_WRITE);
        return adminId;
    }

    private CommerceCatalogGateway requireGateway() {
        if (!commerceCatalogGateway.isEnabled()) {
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce integration is disabled; catalog is unavailable");
        }
        return commerceCatalogGateway;
    }

    private void logSuccess(UUID adminId, AdminActionType actionType, String targetType, String targetId) {
        adminActionAuditLogger.logSuccess(
                adminId,
                actionType.name(),
                targetType,
                targetId,
                "Catalog updated",
                Map.of("target_id", targetId),
                Map.of()
        );
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }
}
