package com.twohands.commerce_service.application.order.viewordersforsupport;

import com.twohands.commerce_service.domain.order.OrderSupportListPagedResult;
import com.twohands.commerce_service.domain.order.OrderSupportListQueryPolicy;
import com.twohands.commerce_service.domain.order.OrderSupportListSearchCriteria;
import com.twohands.commerce_service.domain.order.ViewOrdersForSupportRepository;
import com.twohands.commerce_service.domain.support.WebhookSupportPageRequest;
import com.twohands.commerce_service.domain.support.WebhookSupportPaginationPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ViewOrdersForSupportUseCase {

    private final ViewOrdersForSupportRepository viewOrdersForSupportRepository;

    public ViewOrdersForSupportUseCase(ViewOrdersForSupportRepository viewOrdersForSupportRepository) {
        this.viewOrdersForSupportRepository = viewOrdersForSupportRepository;
    }

    @Transactional(readOnly = true)
    public ViewOrdersForSupportResult execute(ViewOrdersForSupportQuery query) {
        Instant from = OrderSupportListQueryPolicy.parseInstant(query.from(), "from");
        Instant to = OrderSupportListQueryPolicy.parseInstant(query.to(), "to");
        OrderSupportListQueryPolicy.validateDateRange(from, to);

        OrderSupportListSearchCriteria criteria = new OrderSupportListSearchCriteria(
                OrderSupportListQueryPolicy.parseStatus(query.status()),
                OrderSupportListQueryPolicy.parsePaymentMethod(query.paymentMethod()),
                OrderSupportListQueryPolicy.parsePaymentStatus(query.paymentStatus()),
                OrderSupportListQueryPolicy.parseSearchQuery(query.q()),
                from,
                to,
                OrderSupportListQueryPolicy.parseSortField(query.sort())
        );

        WebhookSupportPageRequest pageRequest = WebhookSupportPaginationPolicy.normalize(query.page(), query.size());
        OrderSupportListPagedResult page = viewOrdersForSupportRepository.search(criteria, pageRequest);

        return new ViewOrdersForSupportResult(
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.items()
        );
    }

    public String successMessage() {
        return "Orders retrieved successfully";
    }
}
