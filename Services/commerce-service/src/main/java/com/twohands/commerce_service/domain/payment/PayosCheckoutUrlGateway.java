package com.twohands.commerce_service.domain.payment;

public interface PayosCheckoutUrlGateway {

    PayosPaymentLinkResult createPaymentLink(PayosCreateLinkCommand command);
}
