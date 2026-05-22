package com.twohands.commerce_service.application.product.viewproductlist;

public record ViewProductListCommand(
        Integer page,
        Integer limit,
        String sort
) {
}
