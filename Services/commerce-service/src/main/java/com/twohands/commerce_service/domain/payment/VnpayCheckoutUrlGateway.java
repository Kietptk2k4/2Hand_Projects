package com.twohands.commerce_service.domain.payment;

public interface VnpayCheckoutUrlGateway {

    VnpayPaymentUrlResult createPaymentUrl(VnpayCreatePaymentUrlCommand command);
}
