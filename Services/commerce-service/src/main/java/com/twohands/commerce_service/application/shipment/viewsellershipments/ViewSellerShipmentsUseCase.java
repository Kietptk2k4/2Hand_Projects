package com.twohands.commerce_service.application.shipment.viewsellershipments;

import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.shipment.SellerShipmentListEntry;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipment.ViewSellerShipmentsRepository;
import com.twohands.commerce_service.domain.shipment.ViewSellerShipmentsResult;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ViewSellerShipmentsUseCase {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    private final SellerShopRepository sellerShopRepository;
    private final ViewSellerShipmentsRepository viewSellerShipmentsRepository;

    public ViewSellerShipmentsUseCase(
            SellerShopRepository sellerShopRepository,
            ViewSellerShipmentsRepository viewSellerShipmentsRepository
    ) {
        this.sellerShopRepository = sellerShopRepository;
        this.viewSellerShipmentsRepository = viewSellerShipmentsRepository;
    }

    @Transactional(readOnly = true)
    public ViewSellerShipmentsResult execute(ViewSellerShipmentsCommand command) {
        sellerShopRepository.findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_SHOP_NOT_FOUND));

        PageQuery pageQuery = resolvePageQuery(command.page(), command.limit());
        Optional<ShipmentStatus> status = parseStatusFilter(command.status());
        Optional<String> searchQuery = normalizeSearchQuery(command.searchQuery());

        long totalItems = viewSellerShipmentsRepository.countBySellerId(
                command.sellerId(),
                status,
                searchQuery
        );
        List<SellerShipmentListEntry> items = totalItems == 0
                ? List.of()
                : viewSellerShipmentsRepository.findBySellerId(
                        command.sellerId(),
                        status,
                        searchQuery,
                        pageQuery
                );
        Map<String, Long> statusCounts = viewSellerShipmentsRepository.countByStatusForSeller(command.sellerId());

        return new ViewSellerShipmentsResult(
                items,
                PageMeta.of(pageQuery.page(), pageQuery.limit(), totalItems),
                statusCounts
        );
    }

    public String successMessage() {
        return "Lay danh sach van don seller thanh cong.";
    }

    private PageQuery resolvePageQuery(Integer page, Integer limit) {
        int resolvedPage = page == null ? DEFAULT_PAGE : page;
        int resolvedLimit = limit == null ? DEFAULT_LIMIT : limit;
        if (resolvedPage < 1) {
            throw new AppException(ErrorCode.INVALID_PAGINATION, "page must be >= 1", "page", "must be >= 1");
        }
        if (resolvedLimit < 1 || resolvedLimit > MAX_LIMIT) {
            throw new AppException(
                    ErrorCode.INVALID_PAGINATION,
                    "limit must be between 1 and " + MAX_LIMIT,
                    "limit",
                    "must be between 1 and " + MAX_LIMIT
            );
        }
        return new PageQuery(resolvedPage, resolvedLimit);
    }

    private Optional<ShipmentStatus> parseStatusFilter(String status) {
        if (!StringUtils.hasText(status)) {
            return Optional.empty();
        }
        try {
            return Optional.of(ShipmentStatus.valueOf(status.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "status",
                    "must be one of PENDING, PICKING_UP, READY_TO_SHIP, SHIPPED, DELIVERED, FAILED, CANCELLED, RETURNED"
            );
        }
    }

    private Optional<String> normalizeSearchQuery(String searchQuery) {
        if (!StringUtils.hasText(searchQuery)) {
            return Optional.empty();
        }
        String trimmed = searchQuery.trim();
        return trimmed.isEmpty() ? Optional.empty() : Optional.of(trimmed);
    }
}
