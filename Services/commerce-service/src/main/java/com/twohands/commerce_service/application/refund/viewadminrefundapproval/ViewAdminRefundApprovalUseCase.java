package com.twohands.commerce_service.application.refund.viewadminrefundapproval;

import com.twohands.commerce_service.domain.order.AdminRefundApprovalItem;
import com.twohands.commerce_service.domain.order.AdminRefundApprovalRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewAdminRefundApprovalUseCase {

    private final AdminRefundApprovalRepository adminRefundApprovalRepository;

    public ViewAdminRefundApprovalUseCase(AdminRefundApprovalRepository adminRefundApprovalRepository) {
        this.adminRefundApprovalRepository = adminRefundApprovalRepository;
    }

    @Transactional(readOnly = true)
    public AdminRefundApprovalItem execute(ViewAdminRefundApprovalCommand command) {
        return adminRefundApprovalRepository.findById(command.refundRequestId())
                .orElseThrow(() -> new AppException(ErrorCode.REFUND_REQUEST_NOT_FOUND));
    }

    public String successMessage() {
        return "Lay chi tiet duyet hoan tien thanh cong.";
    }
}
