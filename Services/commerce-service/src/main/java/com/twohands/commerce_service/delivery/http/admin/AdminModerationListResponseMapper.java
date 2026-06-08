package com.twohands.commerce_service.delivery.http.admin;

import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.delivery.http.catalog.PageMetaResponse;
import com.twohands.commerce_service.domain.admin.AdminProductListEntry;
import com.twohands.commerce_service.domain.admin.AdminReviewListEntry;
import com.twohands.commerce_service.domain.admin.AdminShopListEntry;
import com.twohands.commerce_service.domain.admin.ViewAdminProductsForModerationResult;
import com.twohands.commerce_service.domain.admin.ViewAdminReviewsForModerationResult;
import com.twohands.commerce_service.domain.admin.ViewAdminShopsForModerationResult;

final class AdminModerationListResponseMapper {

    private AdminModerationListResponseMapper() {
    }

    static ViewAdminShopsForModerationResponse toShopListResponse(ViewAdminShopsForModerationResult result) {
        return new ViewAdminShopsForModerationResponse(
                result.items().stream().map(AdminModerationListResponseMapper::toShopItem).toList(),
                toPageMeta(result.pagination())
        );
    }

    static ViewAdminProductsForModerationResponse toProductListResponse(
            ViewAdminProductsForModerationResult result
    ) {
        return new ViewAdminProductsForModerationResponse(
                result.items().stream().map(AdminModerationListResponseMapper::toProductItem).toList(),
                toPageMeta(result.pagination())
        );
    }

    static ViewAdminReviewsForModerationResponse toReviewListResponse(
            ViewAdminReviewsForModerationResult result
    ) {
        return new ViewAdminReviewsForModerationResponse(
                result.items().stream().map(AdminModerationListResponseMapper::toReviewItem).toList(),
                toPageMeta(result.pagination())
        );
    }

    private static AdminShopListItemResponse toShopItem(AdminShopListEntry entry) {
        return new AdminShopListItemResponse(
                entry.shopId(),
                entry.sellerId(),
                entry.shopName(),
                entry.logoUrl(),
                entry.status(),
                entry.createdAt()
        );
    }

    private static AdminProductListItemResponse toProductItem(AdminProductListEntry entry) {
        return new AdminProductListItemResponse(
                entry.productId(),
                entry.sellerId(),
                entry.shopId(),
                entry.shopName(),
                entry.title(),
                entry.thumbnailUrl(),
                entry.categoryId(),
                entry.categoryName(),
                entry.price(),
                entry.effectivePrice(),
                entry.status(),
                entry.createdAt(),
                entry.removedAt(),
                entry.removeReason()
        );
    }

    private static AdminReviewListItemResponse toReviewItem(AdminReviewListEntry entry) {
        return new AdminReviewListItemResponse(
                entry.reviewId(),
                entry.orderItemId(),
                entry.productId(),
                entry.productTitle(),
                entry.productThumbnailUrl(),
                entry.buyerId(),
                null,
                null,
                entry.sellerId(),
                entry.rating(),
                entry.comment(),
                entry.status(),
                entry.createdAt()
        );
    }

    private static PageMetaResponse toPageMeta(PageMeta pagination) {
        return new PageMetaResponse(
                pagination.page(),
                pagination.limit(),
                pagination.totalItems(),
                pagination.totalPages(),
                pagination.hasNext()
        );
    }
}
