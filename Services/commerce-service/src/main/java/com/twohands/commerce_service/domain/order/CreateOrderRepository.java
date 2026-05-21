package com.twohands.commerce_service.domain.order;

import java.util.List;

public interface CreateOrderRepository {

    List<CreateOrderItemResult> createOrder(CreateOrderRequest request, OrderStatus initialStatus);
}
