package com.twohands.commerce_service.application.order.processsellerorderitem;

import com.twohands.commerce_service.application.order.common.SellerOrderItemProcessingOutboxService;
import com.twohands.commerce_service.domain.order.OrderItemStatus;
import com.twohands.commerce_service.domain.order.ProcessSellerOrderItemRepository;
import com.twohands.commerce_service.domain.order.ProcessSellerOrderItemResult;
import com.twohands.commerce_service.domain.order.SellerOrderItemLine;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.shipment.CreateShipmentOrderContext;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProcessSellerOrderItemUseCase {

    private final ProcessSellerOrderItemRepository processSellerOrderItemRepository;
    private final SellerShopRepository sellerShopRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final SellerOrderItemProcessingOutboxService sellerOrderItemProcessingOutboxService;
    private final Clock clock;

    public ProcessSellerOrderItemUseCase(
            ProcessSellerOrderItemRepository processSellerOrderItemRepository,
            SellerShopRepository sellerShopRepository,
            OutboxEventRepository outboxEventRepository,
            SellerOrderItemProcessingOutboxService sellerOrderItemProcessingOutboxService,
            Clock clock
    ) {
        this.processSellerOrderItemRepository = processSellerOrderItemRepository;
        this.sellerShopRepository = sellerShopRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.sellerOrderItemProcessingOutboxService = sellerOrderItemProcessingOutboxService;
        this.clock = clock;
    }

    @Transactional
    public ProcessSellerOrderItemResult execute(ProcessSellerOrderItemCommand command) {
        List<UUID> requestedIds = distinctIds(command.orderItemIds());
        if (requestedIds.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "order_item_ids must not be empty");
        }

        sellerShopRepository.findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_SHOP_NOT_FOUND));

        List<SellerOrderItemLine> items = processSellerOrderItemRepository.findOrderItemsBySellerAndIds(
                command.sellerId(),
                requestedIds
        );
        if (items.size() != requestedIds.size()) {
            throw new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        validateItems(items);
        validateOrders(items);

        Instant occurredAt = clock.instant();
        int newlyProcessed = processSellerOrderItemRepository.markPendingItemsProcessing(
                command.sellerId(),
                requestedIds,
                occurredAt
        );

        publishOutboxEvents(command.sellerId(), items, newlyProcessed, occurredAt);

        return buildResult(items, newlyProcessed, occurredAt);
    }

    public String successMessage(int newlyProcessed) {
        return newlyProcessed > 0
                ? "Danh dau chuan bi hang thanh cong."
                : "Order items da o trang thai PROCESSING.";
    }

    private void validateItems(List<SellerOrderItemLine> items) {
        for (SellerOrderItemLine item : items) {
            if (!item.canMarkProcessing()) {
                throw new AppException(
                        ErrorCode.ORDER_ITEM_NOT_PROCESSABLE,
                        "Order item cannot be processed: " + item.status()
                );
            }
        }
    }

    private void validateOrders(List<SellerOrderItemLine> items) {
        Set<UUID> orderIds = items.stream().map(SellerOrderItemLine::orderId).collect(Collectors.toSet());
        for (UUID orderId : orderIds) {
            CreateShipmentOrderContext order = processSellerOrderItemRepository.findOrderContext(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

            if (!"PROCESSING".equals(order.orderStatus())) {
                throw new AppException(ErrorCode.ORDER_NOT_PROCESSING);
            }

            validatePayment(order);
        }
    }

    private void validatePayment(CreateShipmentOrderContext order) {
        if (order.paymentMethod() == PaymentMethod.PAYOS && order.paymentStatus() != PaymentStatus.PAID) {
            throw new AppException(
                    ErrorCode.INVALID_PAYMENT_STATE,
                    "PayOS order must be PAID before processing items"
            );
        }
    }

    private void publishOutboxEvents(
            UUID sellerId,
            List<SellerOrderItemLine> items,
            int newlyProcessed,
            Instant occurredAt
    ) {
        if (newlyProcessed == 0) {
            return;
        }

        Map<UUID, List<UUID>> newlyProcessedByOrder = items.stream()
                .filter(SellerOrderItemLine::isPending)
                .collect(Collectors.groupingBy(
                        SellerOrderItemLine::orderId,
                        Collectors.mapping(SellerOrderItemLine::orderItemId, Collectors.toList())
                ));

        for (Map.Entry<UUID, List<UUID>> entry : newlyProcessedByOrder.entrySet()) {
            outboxEventRepository.save(sellerOrderItemProcessingOutboxService.build(
                    entry.getKey(),
                    sellerId,
                    entry.getValue(),
                    occurredAt
            ));
        }
    }

    private ProcessSellerOrderItemResult buildResult(
            List<SellerOrderItemLine> items,
            int newlyProcessed,
            Instant occurredAt
    ) {
        int alreadyProcessing = (int) items.stream().filter(SellerOrderItemLine::isProcessing).count();
        List<ProcessSellerOrderItemResult.ProcessedOrderItemSummary> summaries = items.stream()
                .map(item -> new ProcessSellerOrderItemResult.ProcessedOrderItemSummary(
                        item.orderItemId(),
                        item.orderId(),
                        OrderItemStatus.PROCESSING,
                        item.productNameSnapshot(),
                        item.quantity(),
                        item.isPending()
                ))
                .toList();

        return new ProcessSellerOrderItemResult(
                summaries,
                newlyProcessed,
                alreadyProcessing,
                occurredAt
        );
    }

    private List<UUID> distinctIds(List<UUID> orderItemIds) {
        if (orderItemIds == null) {
            return List.of();
        }
        return orderItemIds.stream().distinct().toList();
    }
}
