package com.twohands.commerce_service.delivery.http.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.application.catalog.viewactivebrands.ViewActiveBrandsUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.catalog.admin.AdminBrandRow;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/commerce/api/v1/brands")
public class BrandCatalogController {

    private final ViewActiveBrandsUseCase viewActiveBrandsUseCase;

    public BrandCatalogController(ViewActiveBrandsUseCase viewActiveBrandsUseCase) {
        this.viewActiveBrandsUseCase = viewActiveBrandsUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ViewActiveBrandsResponse>> listActiveBrands() {
        List<AdminBrandRow> items = viewActiveBrandsUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewActiveBrandsUseCase.successMessage(),
                new ViewActiveBrandsResponse(items.stream()
                        .map(row -> new ViewActiveBrandsResponse.BrandSummaryResponse(
                                row.id(),
                                row.name(),
                                row.slug()
                        ))
                        .toList())
        ));
    }

    public record ViewActiveBrandsResponse(List<BrandSummaryResponse> items) {
        public record BrandSummaryResponse(
                @JsonProperty("brand_id") java.util.UUID brandId,
                @JsonProperty("brand_name") String brandName,
                @JsonProperty("brand_slug") String brandSlug
        ) {
        }
    }
}
