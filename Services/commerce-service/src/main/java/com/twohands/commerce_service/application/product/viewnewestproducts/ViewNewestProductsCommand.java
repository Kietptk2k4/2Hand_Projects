package com.twohands.commerce_service.application.product.viewnewestproducts;

public record ViewNewestProductsCommand(Integer page, Integer limit, String sort) {
}
