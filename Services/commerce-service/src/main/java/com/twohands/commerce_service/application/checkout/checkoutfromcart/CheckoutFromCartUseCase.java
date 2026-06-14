package com.twohands.commerce_service.application.checkout.checkoutfromcart;

import com.twohands.commerce_service.application.inventory.reserveinventory.ReserveInventoryCommand;
import com.twohands.commerce_service.application.inventory.reserveinventory.ReserveInventoryResult;
import com.twohands.commerce_service.application.inventory.reserveinventory.ReserveInventoryUseCase;
import com.twohands.commerce_service.application.order.common.InventoryReservedOutboxService;
import com.twohands.commerce_service.application.order.createorder.CreateOrderCommand;
import com.twohands.commerce_service.application.order.createorder.CreateOrderUseCase;
import com.twohands.commerce_service.config.CommerceCheckoutProperties;
import com.twohands.commerce_service.domain.checkout.CheckoutFromCartRepository;
import com.twohands.commerce_service.domain.checkout.CheckoutFromCartRequest;
import com.twohands.commerce_service.domain.checkout.CheckoutFromCartResult;
import com.twohands.commerce_service.domain.checkout.CheckoutPaymentMethodPolicy;
import com.twohands.commerce_service.domain.checkout.CheckoutPrepareOutcome;
import com.twohands.commerce_service.domain.checkout.CheckoutPreparedData;
import com.twohands.commerce_service.domain.order.CreateOrderResult;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.application.payment.createvnpaycheckouturl.CreateVnpayCheckoutUrlCommand;
import com.twohands.commerce_service.application.payment.createvnpaycheckouturl.CreateVnpayCheckoutUrlUseCase;
import com.twohands.commerce_service.domain.payment.CreateVnpayCheckoutUrlResult;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CheckoutFromCartUseCase {

    private final CheckoutFromCartRepository checkoutFromCartRepository;
    private final ReserveInventoryUseCase reserveInventoryUseCase;
    private final CreateOrderUseCase createOrderUseCase;
    private final OutboxEventRepository outboxEventRepository;
    private final InventoryReservedOutboxService inventoryReservedOutboxService;
    private final CommerceCheckoutProperties checkoutProperties;
    private final CreateVnpayCheckoutUrlUseCase createVnpayCheckoutUrlUseCase;

    public CheckoutFromCartUseCase(
            CheckoutFromCartRepository checkoutFromCartRepository,
            ReserveInventoryUseCase reserveInventoryUseCase,
            CreateOrderUseCase createOrderUseCase,
            OutboxEventRepository outboxEventRepository,
            InventoryReservedOutboxService inventoryReservedOutboxService,
            CommerceCheckoutProperties checkoutProperties,
            CreateVnpayCheckoutUrlUseCase createVnpayCheckoutUrlUseCase
    ) {
        this.checkoutFromCartRepository = checkoutFromCartRepository;
        this.reserveInventoryUseCase = reserveInventoryUseCase;
        this.createOrderUseCase = createOrderUseCase;
        this.outboxEventRepository = outboxEventRepository;
        this.inventoryReservedOutboxService = inventoryReservedOutboxService;
        this.checkoutProperties = checkoutProperties;
        this.createVnpayCheckoutUrlUseCase = createVnpayCheckoutUrlUseCase;
    }

    @Transactional
    public CheckoutFromCartResult execute(CheckoutFromCartCommand command) {
        CheckoutPaymentMethodPolicy.validateForCheckout(
                command.paymentMethod(),
                checkoutProperties.isCodOnlyEnabled()
        );

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

        ReserveInventoryResult reservation = reserveInventoryUseCase.execute(new ReserveInventoryCommand(
                prepared.reservationLines(),
                prepared.occurredAt()
        ));

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
                reservation.reservedItems(),
                reservation.reservedAt()
        ));

        return new CheckoutFromCartResult(
                orderResult.orderId(),
                orderResult.paymentId(),
                orderResult.paymentMethod(),
                orderResult.paymentStatus(),
                orderResult.status(),
                orderResult.finalAmount(),
                resolvePayosCheckoutUrl(orderResult.paymentMethod()),
                resolveVnpayRedirect(orderResult.paymentMethod(), orderResult.paymentId(), prepared.buyerId()),
                false
        );
    }

    public String successMessage(boolean idempotentReplay) {
        if (idempotentReplay) {
            return "Don hang da duoc tao truoc do (idempotency).";
        }
        return "Checkout thanh cong.";
    }

    private String resolvePayosCheckoutUrl(PaymentMethod paymentMethod) {
        if (paymentMethod != PaymentMethod.PAYOS) {
            return null;
        }
        return null;
    }

    private String resolveVnpayRedirect(PaymentMethod paymentMethod, UUID paymentId, UUID buyerId) {
        if (paymentMethod != PaymentMethod.VNPAY) {
            return null;
        }
        CreateVnpayCheckoutUrlResult result = createVnpayCheckoutUrlUseCase.execute(
                new CreateVnpayCheckoutUrlCommand(paymentId, buyerId, "127.0.0.1")
        );
        return result.checkoutUrl();
    }
}
