package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.application.catalog.admin.createbrand.CreateBrandCommand;
import com.twohands.commerce_service.application.catalog.admin.createbrand.CreateBrandUseCase;
import com.twohands.commerce_service.application.catalog.admin.createcategory.CreateCategoryCommand;
import com.twohands.commerce_service.application.catalog.admin.createcategory.CreateCategoryUseCase;
import com.twohands.commerce_service.application.catalog.admin.listadminbrands.ListAdminBrandsCommand;
import com.twohands.commerce_service.application.catalog.admin.listadminbrands.ListAdminBrandsResult;
import com.twohands.commerce_service.application.catalog.admin.listadminbrands.ListAdminBrandsUseCase;
import com.twohands.commerce_service.application.catalog.admin.listadmincategories.ListAdminCategoriesCommand;
import com.twohands.commerce_service.application.catalog.admin.listadmincategories.ListAdminCategoriesUseCase;
import com.twohands.commerce_service.application.catalog.admin.setbrandactive.SetBrandActiveCommand;
import com.twohands.commerce_service.application.catalog.admin.setbrandactive.SetBrandActiveUseCase;
import com.twohands.commerce_service.application.catalog.admin.setcategoryactive.SetCategoryActiveCommand;
import com.twohands.commerce_service.application.catalog.admin.setcategoryactive.SetCategoryActiveUseCase;
import com.twohands.commerce_service.application.catalog.admin.updatebrand.UpdateBrandCommand;
import com.twohands.commerce_service.application.catalog.admin.updatebrand.UpdateBrandUseCase;
import com.twohands.commerce_service.application.catalog.admin.updatecategory.UpdateCategoryCommand;
import com.twohands.commerce_service.application.catalog.admin.updatecategory.UpdateCategoryUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.catalog.admin.AdminBrandRow;
import com.twohands.commerce_service.domain.catalog.admin.AdminCategoryRow;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import com.twohands.commerce_service.security.CommerceAdminAuthorization;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/admin/catalog")
public class AdminCatalogController {

    private final ListAdminCategoriesUseCase listAdminCategoriesUseCase;
    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final SetCategoryActiveUseCase setCategoryActiveUseCase;
    private final ListAdminBrandsUseCase listAdminBrandsUseCase;
    private final CreateBrandUseCase createBrandUseCase;
    private final UpdateBrandUseCase updateBrandUseCase;
    private final SetBrandActiveUseCase setBrandActiveUseCase;
    private final CommerceAdminAuthorization commerceAdminAuthorization;

    public AdminCatalogController(
            ListAdminCategoriesUseCase listAdminCategoriesUseCase,
            CreateCategoryUseCase createCategoryUseCase,
            UpdateCategoryUseCase updateCategoryUseCase,
            SetCategoryActiveUseCase setCategoryActiveUseCase,
            ListAdminBrandsUseCase listAdminBrandsUseCase,
            CreateBrandUseCase createBrandUseCase,
            UpdateBrandUseCase updateBrandUseCase,
            SetBrandActiveUseCase setBrandActiveUseCase,
            CommerceAdminAuthorization commerceAdminAuthorization
    ) {
        this.listAdminCategoriesUseCase = listAdminCategoriesUseCase;
        this.createCategoryUseCase = createCategoryUseCase;
        this.updateCategoryUseCase = updateCategoryUseCase;
        this.setCategoryActiveUseCase = setCategoryActiveUseCase;
        this.listAdminBrandsUseCase = listAdminBrandsUseCase;
        this.createBrandUseCase = createBrandUseCase;
        this.updateBrandUseCase = updateBrandUseCase;
        this.setBrandActiveUseCase = setBrandActiveUseCase;
        this.commerceAdminAuthorization = commerceAdminAuthorization;
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<AdminCatalogListCategoriesResponse>> listCategories(
            @RequestParam(name = "is_active", required = false) Boolean isActive,
            @RequestParam(required = false) String q,
            Authentication authentication
    ) {
        requireCatalogRead(authentication);
        List<AdminCategoryRow> items = listAdminCategoriesUseCase.execute(
                new ListAdminCategoriesCommand(isActive, q)
        );
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                listAdminCategoriesUseCase.successMessage(),
                new AdminCatalogListCategoriesResponse(items.stream()
                        .map(AdminCatalogMapper::toCategoryResponse)
                        .toList())
        ));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<AdminCatalogMapper.AdminCategoryResponse>> createCategory(
            @Valid @RequestBody AdminCatalogCategoryRequest request,
            Authentication authentication
    ) {
        requireCatalogWrite(authentication);
        AdminCategoryRow created = createCategoryUseCase.execute(new CreateCategoryCommand(
                request.name(),
                request.slug(),
                request.parentId()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                HttpStatus.CREATED.value(),
                createCategoryUseCase.successMessage(),
                AdminCatalogMapper.toCategoryResponse(created)
        ));
    }

    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<ApiResponse<AdminCatalogMapper.AdminCategoryResponse>> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody AdminCatalogCategoryRequest request,
            Authentication authentication
    ) {
        requireCatalogWrite(authentication);
        AdminCategoryRow updated = updateCategoryUseCase.execute(new UpdateCategoryCommand(
                categoryId,
                request.name(),
                request.slug(),
                request.parentId()
        ));
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                updateCategoryUseCase.successMessage(),
                AdminCatalogMapper.toCategoryResponse(updated)
        ));
    }

    @PostMapping("/categories/{categoryId}/activate")
    public ResponseEntity<ApiResponse<AdminCatalogMapper.AdminCategoryResponse>> activateCategory(
            @PathVariable UUID categoryId,
            Authentication authentication
    ) {
        requireCatalogWrite(authentication);
        AdminCategoryRow updated = setCategoryActiveUseCase.execute(new SetCategoryActiveCommand(categoryId, true));
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                setCategoryActiveUseCase.successMessage(),
                AdminCatalogMapper.toCategoryResponse(updated)
        ));
    }

    @PostMapping("/categories/{categoryId}/deactivate")
    public ResponseEntity<ApiResponse<AdminCatalogMapper.AdminCategoryResponse>> deactivateCategory(
            @PathVariable UUID categoryId,
            Authentication authentication
    ) {
        requireCatalogWrite(authentication);
        AdminCategoryRow updated = setCategoryActiveUseCase.execute(new SetCategoryActiveCommand(categoryId, false));
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                setCategoryActiveUseCase.successMessage(),
                AdminCatalogMapper.toCategoryResponse(updated)
        ));
    }

    @GetMapping("/brands")
    public ResponseEntity<ApiResponse<AdminCatalogListBrandsResponse>> listBrands(
            @RequestParam(name = "is_active", required = false) Boolean isActive,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            Authentication authentication
    ) {
        requireCatalogRead(authentication);
        ListAdminBrandsResult result = listAdminBrandsUseCase.execute(
                new ListAdminBrandsCommand(isActive, q, page, limit)
        );
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                listAdminBrandsUseCase.successMessage(),
                AdminCatalogListBrandsResponse.from(result)
        ));
    }

    @PostMapping("/brands")
    public ResponseEntity<ApiResponse<AdminCatalogMapper.AdminBrandResponse>> createBrand(
            @Valid @RequestBody AdminCatalogBrandRequest request,
            Authentication authentication
    ) {
        requireCatalogWrite(authentication);
        AdminBrandRow created = createBrandUseCase.execute(new CreateBrandCommand(request.name(), request.slug()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                HttpStatus.CREATED.value(),
                createBrandUseCase.successMessage(),
                AdminCatalogMapper.toBrandResponse(created)
        ));
    }

    @PutMapping("/brands/{brandId}")
    public ResponseEntity<ApiResponse<AdminCatalogMapper.AdminBrandResponse>> updateBrand(
            @PathVariable UUID brandId,
            @Valid @RequestBody AdminCatalogBrandRequest request,
            Authentication authentication
    ) {
        requireCatalogWrite(authentication);
        AdminBrandRow updated = updateBrandUseCase.execute(new UpdateBrandCommand(
                brandId,
                request.name(),
                request.slug()
        ));
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                updateBrandUseCase.successMessage(),
                AdminCatalogMapper.toBrandResponse(updated)
        ));
    }

    @PostMapping("/brands/{brandId}/activate")
    public ResponseEntity<ApiResponse<AdminCatalogMapper.AdminBrandResponse>> activateBrand(
            @PathVariable UUID brandId,
            Authentication authentication
    ) {
        requireCatalogWrite(authentication);
        AdminBrandRow updated = setBrandActiveUseCase.execute(new SetBrandActiveCommand(brandId, true));
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                setBrandActiveUseCase.successMessage(),
                AdminCatalogMapper.toBrandResponse(updated)
        ));
    }

    @PostMapping("/brands/{brandId}/deactivate")
    public ResponseEntity<ApiResponse<AdminCatalogMapper.AdminBrandResponse>> deactivateBrand(
            @PathVariable UUID brandId,
            Authentication authentication
    ) {
        requireCatalogWrite(authentication);
        AdminBrandRow updated = setBrandActiveUseCase.execute(new SetBrandActiveCommand(brandId, false));
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                setBrandActiveUseCase.successMessage(),
                AdminCatalogMapper.toBrandResponse(updated)
        ));
    }

    private void requireCatalogRead(Authentication authentication) {
        commerceAdminAuthorization.requirePermission(
                resolveAuthenticatedUser(authentication),
                CommerceAdminAuthorization.PERMISSION_CATALOG_READ
        );
    }

    private void requireCatalogWrite(Authentication authentication) {
        commerceAdminAuthorization.requirePermission(
                resolveAuthenticatedUser(authentication),
                CommerceAdminAuthorization.PERMISSION_CATALOG_WRITE
        );
    }

    private AuthenticatedUser resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal;
    }
}
