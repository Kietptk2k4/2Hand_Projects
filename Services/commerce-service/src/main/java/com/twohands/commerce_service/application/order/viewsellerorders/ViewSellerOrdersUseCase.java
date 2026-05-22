package com.twohands.commerce_service.application.order.viewsellerorders;

import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.order.OrderItemStatus;
import com.twohands.commerce_service.domain.order.SellerOrderListEntry;
import com.twohands.commerce_service.domain.order.ViewSellerOrdersRepository;
import com.twohands.commerce_service.domain.order.ViewSellerOrdersResult;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class ViewSellerOrdersUseCase {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    private final SellerShopRepository sellerShopRepository;
    private final ViewSellerOrdersRepository viewSellerOrdersRepository;

    public ViewSellerOrdersUseCase(
            SellerShopRepository sellerShopRepository,
            ViewSellerOrdersRepository viewSellerOrdersRepository
    ) {
        this.sellerShopRepository = sellerShopRepository;
        this.viewSellerOrdersRepository = viewSellerOrdersRepository;
    }

    @Transactional(readOnly = true)
    public ViewSellerOrdersResult execute(ViewSellerOrdersCommand command) {
        sellerShopRepository.findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_SHOP_NOT_FOUND));

        PageQuery pageQuery = resolvePageQuery(command.page(), command.limit());
        Optional<OrderItemStatus> itemStatus = parseItemStatusFilter(command.status());
        Optional<ShipmentStatus> shipmentStatus = parseShipmentStatusFilter(command.shipmentStatus());

        long totalItems = viewSellerOrdersRepository.countBySellerId(
                command.sellerId(),
                itemStatus,
                shipmentStatus
        );
        List<SellerOrderListEntry> items = totalItems == 0
                ? List.of()
                : viewSellerOrdersRepository.findBySellerId(
                        command.sellerId(),
                        itemStatus,
                        shipmentStatus,
                        pageQuery
                );

        return new ViewSellerOrdersResult(items, PageMeta.of(pageQuery.page(), pageQuery.limit(), totalItems));
    }

    public String successMessage() {
        return "Lay danh sach don hang seller thanh cong.";
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

    private Optional<OrderItemStatus> parseItemStatusFilter(String status) {
        if (!StringUtils.hasText(status)) {
            return Optional.empty();
        }
        try {
            return Optional.of(OrderItemStatus.valueOf(status.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "status",
                    "must be one of PENDING, PROCESSING, SHIPPED, DELIVERED, COMPLETED, CANCELLED, FAILED, RETURNED"
            );
        }
    }

    private Optional<ShipmentStatus> parseShipmentStatusFilter(String shipmentStatus) {
        if (!StringUtils.hasText(shipmentStatus)) {
            return Optional.empty();
        }
        try {
            return Optional.of(ShipmentStatus.valueOf(shipmentStatus.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "shipment_status",
                    "must be one of PENDING, PICKING_UP, READY_TO_SHIP, SHIPPED, DELIVERED, FAILED, CANCELLED, RETURNED"
            );
        }
    }
}
