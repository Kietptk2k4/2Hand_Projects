package com.twohands.commerce_service.application.product.searchproduct;

public record SearchProductCommand(
        String keyword,
        Integer page,
        Integer limit,
        String sort
) {
}
