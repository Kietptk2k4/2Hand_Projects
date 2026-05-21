package com.twohands.commerce_service.application.order.autocompletedeliveredorder;

import com.twohands.commerce_service.domain.order.DeliveredOrderCompletionRepository;
import com.twohands.commerce_service.domain.order.DeliveredOrderCompletionResult;
import com.twohands.commerce_service.domain.order.StaleDeliveredOrderItemCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AutoCompleteDeliveredOrdersUseCase {

    private static final Logger log = LoggerFactory.getLogger(AutoCompleteDeliveredOrdersUseCase.class);

    private final DeliveredOrderCompletionRepository deliveredOrderCompletionRepository;
    private final int batchSize;
    private final int completionWindowDays;

    public AutoCompleteDeliveredOrdersUseCase(
            DeliveredOrderCompletionRepository deliveredOrderCompletionRepository,
            @Value("${commerce.jobs.auto-complete-delivered-order.batch-size:50}") int batchSize,
            @Value("${commerce.jobs.auto-complete-delivered-order.completion-window-days:7}") int completionWindowDays
    ) {
        this.deliveredOrderCompletionRepository = deliveredOrderCompletionRepository;
        this.batchSize = batchSize;
        this.completionWindowDays = completionWindowDays;
    }

    public AutoCompleteDeliveredOrdersResult execute() {
        Instant now = Instant.now();
        Instant deliveredBefore = now.minus(completionWindowDays, ChronoUnit.DAYS);

        List<StaleDeliveredOrderItemCandidate> candidates =
                deliveredOrderCompletionRepository.findStaleDeliveredItems(batchSize, deliveredBefore);

        Map<UUID, List<UUID>> itemsByOrder = groupByOrder(candidates);

        int itemsCompleted = 0;
        int ordersCompleted = 0;
        int failed = 0;

        for (Map.Entry<UUID, List<UUID>> entry : itemsByOrder.entrySet()) {
            try {
                DeliveredOrderCompletionResult result = deliveredOrderCompletionRepository
                        .completeDeliveredItemsForOrder(entry.getKey(), entry.getValue(), now);
                itemsCompleted += result.itemsCompleted();
                if (result.orderCompleted()) {
                    ordersCompleted++;
                }
            } catch (Exception ex) {
                failed++;
                log.error("Auto-complete delivered order failed for orderId={}", entry.getKey(), ex);
            }
        }

        return new AutoCompleteDeliveredOrdersResult(
                candidates.size(),
                itemsCompleted,
                ordersCompleted,
                itemsByOrder.size(),
                failed
        );
    }

    private Map<UUID, List<UUID>> groupByOrder(List<StaleDeliveredOrderItemCandidate> candidates) {
        Map<UUID, List<UUID>> grouped = new LinkedHashMap<>();
        for (StaleDeliveredOrderItemCandidate candidate : candidates) {
            grouped.computeIfAbsent(candidate.orderId(), ignored -> new ArrayList<>())
                    .add(candidate.orderItemId());
        }
        return grouped;
    }
}
