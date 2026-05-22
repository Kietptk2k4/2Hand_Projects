package com.twohands.commerce_service.application.order.vieworderlist;

import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.order.OrderListEntry;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.order.ViewOrderListRepository;
import com.twohands.commerce_service.domain.order.ViewOrderListResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class ViewOrderListUseCase {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    private final ViewOrderListRepository viewOrderListRepository;

    public ViewOrderListUseCase(ViewOrderListRepository viewOrderListRepository) {
        this.viewOrderListRepository = viewOrderListRepository;
    }

    @Transactional(readOnly = true)
    public ViewOrderListResult execute(ViewOrderListCommand command) {
        PageQuery pageQuery = resolvePageQuery(command.page(), command.limit());
        Optional<OrderStatus> statusFilter = parseStatusFilter(command.status());

        long totalItems = viewOrderListRepository.countByBuyerId(command.buyerId(), statusFilter);
        List<OrderListEntry> orders = totalItems == 0
                ? List.of()
                : viewOrderListRepository.findByBuyerId(command.buyerId(), statusFilter, pageQuery);

        return new ViewOrderListResult(orders, PageMeta.of(pageQuery.page(), pageQuery.limit(), totalItems));
    }

    public String successMessage() {
        return "Lay danh sach don hang thanh cong.";
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

    private Optional<OrderStatus> parseStatusFilter(String status) {
        if (!StringUtils.hasText(status)) {
            return Optional.empty();
        }
        try {
            return Optional.of(OrderStatus.valueOf(status.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "status",
                    "must be one of CREATED, AWAITING_PAYMENT, PROCESSING, COMPLETED, CANCELLED"
            );
        }
    }
}
