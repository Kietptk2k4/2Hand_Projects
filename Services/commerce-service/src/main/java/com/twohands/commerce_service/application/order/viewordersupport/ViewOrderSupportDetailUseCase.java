package com.twohands.commerce_service.application.order.viewordersupport;

import com.twohands.commerce_service.domain.order.ViewOrderDetailRepository;
import com.twohands.commerce_service.domain.order.ViewOrderDetailResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewOrderSupportDetailUseCase {

	private final ViewOrderDetailRepository viewOrderDetailRepository;

	public ViewOrderSupportDetailUseCase(ViewOrderDetailRepository viewOrderDetailRepository) {
		this.viewOrderDetailRepository = viewOrderDetailRepository;
	}

	@Transactional(readOnly = true)
	public ViewOrderDetailResult execute(ViewOrderSupportDetailCommand command) {
		return viewOrderDetailRepository
				.findByOrderId(command.orderId())
				.orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
	}

	public String successMessage() {
		return "Order support detail retrieved successfully";
	}
}
