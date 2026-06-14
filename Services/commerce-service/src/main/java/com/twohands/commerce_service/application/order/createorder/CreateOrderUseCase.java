package com.twohands.commerce_service.application.order.createorder;

import com.twohands.commerce_service.application.order.common.OrderCreatedOutboxService;
import com.twohands.commerce_service.application.payment.createpayment.CreatePaymentCommand;
import com.twohands.commerce_service.application.payment.createpayment.CreatePaymentUseCase;
import com.twohands.commerce_service.domain.order.CreateOrderItemResult;
import com.twohands.commerce_service.domain.order.CreateOrderLineRequest;
import com.twohands.commerce_service.domain.order.CreateOrderRepository;
import com.twohands.commerce_service.domain.order.CreateOrderRequest;
import com.twohands.commerce_service.domain.order.CreateOrderResult;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.payment.CreatePaymentResult;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CreateOrderUseCase {

    private final CreateOrderRepository createOrderRepository;
    private final CreatePaymentUseCase createPaymentUseCase;
    private final OutboxEventRepository outboxEventRepository;
    private final OrderCreatedOutboxService orderCreatedOutboxService;

    public CreateOrderUseCase(
            CreateOrderRepository createOrderRepository,
            CreatePaymentUseCase createPaymentUseCase,
            OutboxEventRepository outboxEventRepository,
            OrderCreatedOutboxService orderCreatedOutboxService
    ) {
        this.createOrderRepository = createOrderRepository;
        this.createPaymentUseCase = createPaymentUseCase;
        this.outboxEventRepository = outboxEventRepository;
        this.orderCreatedOutboxService = orderCreatedOutboxService;
    }

    @Transactional
    public CreateOrderResult execute(CreateOrderCommand command) {
        validateAmounts(command.totalAmount(), command.finalAmount());
        validateLines(command.lines());

        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        OrderStatus orderStatus = resolveInitialOrderStatus(command.paymentMethod());

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                orderId,
                command.buyerId(),
                command.totalAmount(),
                command.finalAmount(),
                command.paymentMethod(),
                command.idempotencyKey(),
                command.lines(),
                command.occurredAt()
        );

        List<CreateOrderItemResult> orderItems = createOrderRepository.createOrder(orderRequest, orderStatus);

        CreatePaymentResult payment = createPaymentUseCase.execute(new CreatePaymentCommand(
                paymentId,
                orderId,
                command.buyerId(),
                command.buyerId(),
                command.finalAmount(),
                command.finalAmount(),
                command.paymentMethod(),
                command.paymentMethod(),
                "VND",
                command.idempotencyKey(),
                command.occurredAt()
        ));

        outboxEventRepository.save(orderCreatedOutboxService.build(
                orderId,
                command.buyerId(),
                distinctSellerIds(orderItems),
                command.finalAmount(),
                command.paymentMethod().name(),
                command.occurredAt()
        ));

        return new CreateOrderResult(
                orderId,
                payment.paymentId(),
                orderStatus,
                payment.status(),
                command.paymentMethod(),
                command.totalAmount(),
                command.finalAmount(),
                orderItems
        );
    }

    public OrderStatus resolveInitialOrderStatus(PaymentMethod paymentMethod) {
        if (paymentMethod == PaymentMethod.PAYOS || paymentMethod == PaymentMethod.VNPAY) {
            return OrderStatus.AWAITING_PAYMENT;
        }
        return OrderStatus.PROCESSING;
    }

    private List<UUID> distinctSellerIds(List<CreateOrderItemResult> orderItems) {
        Set<UUID> sellerIds = new LinkedHashSet<>();
        for (CreateOrderItemResult item : orderItems) {
            if (item.sellerId() != null) {
                sellerIds.add(item.sellerId());
            }
        }
        return new ArrayList<>(sellerIds);
    }

    private void validateAmounts(BigDecimal totalAmount, BigDecimal finalAmount) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "total_amount must be >= 0", "total_amount", ">= 0");
        }
        if (finalAmount == null || finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "final_amount must be >= 0", "final_amount", ">= 0");
        }
    }

    private void validateLines(List<CreateOrderLineRequest> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "At least one order line is required",
                    "lines",
                    "must not be empty"
            );
        }
        for (CreateOrderLineRequest line : lines) {
            if (line.productId() == null || line.sellerId() == null) {
                throw new AppException(ErrorCode.ORDER_SNAPSHOT_INCOMPLETE, "Product and seller are required on order line");
            }
            if (line.quantity() <= 0) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Order line quantity must be greater than 0");
            }
            if (line.unitPrice() == null || line.lineTotal() == null) {
                throw new AppException(ErrorCode.ORDER_SNAPSHOT_INCOMPLETE, "Price snapshot is required on order line");
            }
            if (!StringUtils.hasText(line.productName()) || !StringUtils.hasText(line.shopName())) {
                throw new AppException(ErrorCode.ORDER_SNAPSHOT_INCOMPLETE, "Product and shop name snapshots are required");
            }
        }
    }
}
