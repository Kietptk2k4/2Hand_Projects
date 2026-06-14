package com.twohands.admin_service.delivery.http.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.application.catalog.AdminCatalogProxyUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/catalog")
public class AdminCatalogController {

    private final AdminCatalogProxyUseCase adminCatalogProxyUseCase;

    public AdminCatalogController(AdminCatalogProxyUseCase adminCatalogProxyUseCase) {
        this.adminCatalogProxyUseCase = adminCatalogProxyUseCase;
    }

    @GetMapping("/categories")
    @RequireAdminPermission(AdminPermission.CATALOG_READ)
    public ResponseEntity<ApiResponse<JsonNode>> listCategories(
            @RequestParam(name = "is_active", required = false) Boolean isActive,
            @RequestParam(required = false) String q,
            HttpServletRequest request
    ) {
        JsonNode data = adminCatalogProxyUseCase.listCategories(isActive, q, resolveBearerToken(request));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Categories loaded successfully", data));
    }

    @PostMapping("/categories")
    @RequireAdminPermission(AdminPermission.CATALOG_WRITE)
    public ResponseEntity<ApiResponse<JsonNode>> createCategory(
            @Valid @RequestBody AdminCatalogCategoryRequest body,
            HttpServletRequest request
    ) {
        JsonNode data = adminCatalogProxyUseCase.createCategory(toCategoryBody(body), resolveBearerToken(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Category created successfully", data));
    }

    @PutMapping("/categories/{categoryId}")
    @RequireAdminPermission(AdminPermission.CATALOG_WRITE)
    public ResponseEntity<ApiResponse<JsonNode>> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody AdminCatalogCategoryRequest body,
            HttpServletRequest request
    ) {
        JsonNode data = adminCatalogProxyUseCase.updateCategory(categoryId, toCategoryBody(body), resolveBearerToken(request));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Category updated successfully", data));
    }

    @PostMapping("/categories/{categoryId}/activate")
    @RequireAdminPermission(AdminPermission.CATALOG_WRITE)
    public ResponseEntity<ApiResponse<JsonNode>> activateCategory(
            @PathVariable UUID categoryId,
            HttpServletRequest request
    ) {
        JsonNode data = adminCatalogProxyUseCase.setCategoryActive(categoryId, true, resolveBearerToken(request));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Category activated successfully", data));
    }

    @PostMapping("/categories/{categoryId}/deactivate")
    @RequireAdminPermission(AdminPermission.CATALOG_WRITE)
    public ResponseEntity<ApiResponse<JsonNode>> deactivateCategory(
            @PathVariable UUID categoryId,
            HttpServletRequest request
    ) {
        JsonNode data = adminCatalogProxyUseCase.setCategoryActive(categoryId, false, resolveBearerToken(request));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Category deactivated successfully", data));
    }

    @GetMapping("/brands")
    @RequireAdminPermission(AdminPermission.CATALOG_READ)
    public ResponseEntity<ApiResponse<JsonNode>> listBrands(
            @RequestParam(name = "is_active", required = false) Boolean isActive,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            HttpServletRequest request
    ) {
        JsonNode data = adminCatalogProxyUseCase.listBrands(isActive, q, page, limit, resolveBearerToken(request));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Brands loaded successfully", data));
    }

    @PostMapping("/brands")
    @RequireAdminPermission(AdminPermission.CATALOG_WRITE)
    public ResponseEntity<ApiResponse<JsonNode>> createBrand(
            @Valid @RequestBody AdminCatalogBrandRequest body,
            HttpServletRequest request
    ) {
        JsonNode data = adminCatalogProxyUseCase.createBrand(toBrandBody(body), resolveBearerToken(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Brand created successfully", data));
    }

    @PutMapping("/brands/{brandId}")
    @RequireAdminPermission(AdminPermission.CATALOG_WRITE)
    public ResponseEntity<ApiResponse<JsonNode>> updateBrand(
            @PathVariable UUID brandId,
            @Valid @RequestBody AdminCatalogBrandRequest body,
            HttpServletRequest request
    ) {
        JsonNode data = adminCatalogProxyUseCase.updateBrand(brandId, toBrandBody(body), resolveBearerToken(request));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Brand updated successfully", data));
    }

    @PostMapping("/brands/{brandId}/activate")
    @RequireAdminPermission(AdminPermission.CATALOG_WRITE)
    public ResponseEntity<ApiResponse<JsonNode>> activateBrand(
            @PathVariable UUID brandId,
            HttpServletRequest request
    ) {
        JsonNode data = adminCatalogProxyUseCase.setBrandActive(brandId, true, resolveBearerToken(request));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Brand activated successfully", data));
    }

    @PostMapping("/brands/{brandId}/deactivate")
    @RequireAdminPermission(AdminPermission.CATALOG_WRITE)
    public ResponseEntity<ApiResponse<JsonNode>> deactivateBrand(
            @PathVariable UUID brandId,
            HttpServletRequest request
    ) {
        JsonNode data = adminCatalogProxyUseCase.setBrandActive(brandId, false, resolveBearerToken(request));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Brand deactivated successfully", data));
    }

    private Map<String, Object> toCategoryBody(AdminCatalogCategoryRequest body) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", body.name());
        if (body.slug() != null && !body.slug().isBlank()) {
            payload.put("slug", body.slug());
        }
        if (body.parentId() != null) {
            payload.put("parent_id", body.parentId().toString());
        }
        return payload;
    }

    private Map<String, Object> toBrandBody(AdminCatalogBrandRequest body) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", body.name());
        if (body.slug() != null && !body.slug().isBlank()) {
            payload.put("slug", body.slug());
        }
        return payload;
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return "";
        }
        return authorization.substring(7).trim();
    }
}
