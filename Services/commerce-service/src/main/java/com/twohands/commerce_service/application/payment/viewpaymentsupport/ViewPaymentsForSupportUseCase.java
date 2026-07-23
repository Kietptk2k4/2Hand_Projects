package com.twohands.commerce_service.application.payment.viewpaymentsupport;

import com.twohands.commerce_service.domain.payment.PaymentSupportPagedResult;
import com.twohands.commerce_service.domain.payment.PaymentSupportQueryPolicy;
import com.twohands.commerce_service.domain.payment.PaymentSupportSearchCriteria;
import com.twohands.commerce_service.domain.payment.ViewPaymentsForSupportRepository;
import com.twohands.commerce_service.domain.support.WebhookSupportPageRequest;
import com.twohands.commerce_service.domain.support.WebhookSupportPaginationPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ViewPaymentsForSupportUseCase {

    private final ViewPaymentsForSupportRepository viewPaymentsForSupportRepository;

    public ViewPaymentsForSupportUseCase(ViewPaymentsForSupportRepository viewPaymentsForSupportRepository) {
        this.viewPaymentsForSupportRepository = viewPaymentsForSupportRepository;
    }

    @Transactional(readOnly = true)
    public ViewPaymentsForSupportResult execute(ViewPaymentsForSupportQuery query) {
        Instant from = PaymentSupportQueryPolicy.parseInstant(query.from(), "from");
        Instant to = PaymentSupportQueryPolicy.parseInstant(query.to(), "to");
        PaymentSupportQueryPolicy.validateDateRange(from, to);

        PaymentSupportSearchCriteria criteria = new PaymentSupportSearchCriteria(
                PaymentSupportQueryPolicy.normalizeStatus(query.status()),
                PaymentSupportQueryPolicy.normalizePaymentMethod(query.paymentMethod()),
                PaymentSupportQueryPolicy.normalizeOrderId(query.orderId()),
                PaymentSupportQueryPolicy.parseSearchQuery(query.q()).orElse(null),
                PaymentSupportQueryPolicy.normalizeReconciliationStatus(query.reconciliationStatus()),
                from,
                to
        );

        WebhookSupportPageRequest pageRequest = WebhookSupportPaginationPolicy.normalize(query.page(), query.size());
        PaymentSupportPagedResult page = viewPaymentsForSupportRepository.search(criteria, pageRequest);

        return new ViewPaymentsForSupportResult(
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.items()
        );
    }

    public String successMessage() {
        return "Payments retrieved successfully";
    }
}
