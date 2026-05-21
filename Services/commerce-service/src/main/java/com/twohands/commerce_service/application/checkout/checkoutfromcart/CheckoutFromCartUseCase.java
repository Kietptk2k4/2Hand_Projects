package com.twohands.commerce_service.application.checkout.checkoutfromcart;

import com.twohands.commerce_service.application.order.common.InventoryReservedOutboxService;
import com.twohands.commerce_service.application.order.createorder.CreateOrderCommand;
import com.twohands.commerce_service.application.order.createorder.CreateOrderUseCase;
import com.twohands.commerce_service.domain.checkout.CheckoutFromCartRepository;
import com.twohands.commerce_service.domain.checkout.CheckoutFromCartRequest;
import com.twohands.commerce_service.domain.checkout.CheckoutFromCartResult;
import com.twohands.commerce_service.domain.checkout.CheckoutPrepareOutcome;
import com.twohands.commerce_service.domain.checkout.CheckoutPreparedData;
import com.twohands.commerce_service.domain.order.CreateOrderResult;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutFromCartUseCase {

    private final CheckoutFromCartRepository checkoutFromCartRepository;
    private final CreateOrderUseCase createOrderUseCase;
    private final OutboxEventRepository outboxEventRepository;
    private final InventoryReservedOutboxService inventoryReservedOutboxService;

    public CheckoutFromCartUseCase(
            CheckoutFromCartRepository checkoutFromCartRepository,
            CreateOrderUseCase createOrderUseCase,
            OutboxEventRepository outboxEventRepository,
            InventoryReservedOutboxService inventoryReservedOutboxService
    ) {
        this.checkoutFromCartRepository = checkoutFromCartRepository;
        this.createOrderUseCase = createOrderUseCase;
        this.outboxEventRepository = outboxEventRepository;
        this.inventoryReservedOutboxService = inventoryReservedOutboxService;
    }

    @Transactional
    public CheckoutFromCartResult execute(CheckoutFromCartCommand command) {
        validatePaymentMethod(command.paymentMethod());

        CheckoutPrepareOutcome outcome = checkoutFromCartRepository.prepareCheckout(new CheckoutFromCartRequest(
                command.buyerId(),
                command.cartItemIds(),
                command.addressId(),
                command.paymentMethod(),
                command.shipmentType(),
                command.idempotencyKey()
        ));

        if (outcome.idempotentResult().isPresent()) {
            return outcome.idempotentResult().get();
        }

        CheckoutPreparedData prepared = outcome.preparedData()
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_ERROR, "Checkout preparation did not return data"));

        CreateOrderResult orderResult = createOrderUseCase.execute(new CreateOrderCommand(
                prepared.buyerId(),
                prepared.totalAmount(),
                prepared.finalAmount(),
                prepared.paymentMethod(),
                prepared.idempotencyKey(),
                prepared.lines(),
                prepared.occurredAt()
        ));

        outboxEventRepository.save(inventoryReservedOutboxService.build(
                orderResult.orderId(),
                prepared.reservedItems(),
                prepared.occurredAt()
        ));

        return new CheckoutFromCartResult(
                orderResult.orderId(),
                orderResult.paymentId(),
                orderResult.paymentMethod(),
                orderResult.paymentStatus(),
                orderResult.status(),
                orderResult.finalAmount(),
                resolvePayosCheckoutUrl(orderResult.paymentMethod()),
                false
        );
    }

    public String successMessage(boolean idempotentReplay) {
        if (idempotentReplay) {
            return "Don hang da duoc tao truoc do (idempotency).";
        }
        return "Checkout thanh cong.";
    }

    private void validatePaymentMethod(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Payment method is required",
                    "payment_method",
                    "must not be null"
            );
        }
        if (paymentMethod != PaymentMethod.COD && paymentMethod != PaymentMethod.PAYOS) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_METHOD);
        }
    }

    private String resolvePayosCheckoutUrl(PaymentMethod paymentMethod) {
        if (paymentMethod != PaymentMethod.PAYOS) {
            return null;
        }
        return null;
    }
}
