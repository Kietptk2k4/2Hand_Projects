package com.twohands.commerce_service.application.refund.confirmrefundapproval;

import com.twohands.commerce_service.domain.order.AdminRefundApprovalItem;
import com.twohands.commerce_service.domain.order.AdminRefundApprovalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ConfirmRefundApprovalUseCase {

    private final AdminRefundApprovalRepository adminRefundApprovalRepository;

    public ConfirmRefundApprovalUseCase(AdminRefundApprovalRepository adminRefundApprovalRepository) {
        this.adminRefundApprovalRepository = adminRefundApprovalRepository;
    }

    @Transactional
    public AdminRefundApprovalItem execute(ConfirmRefundApprovalCommand command) {
        return adminRefundApprovalRepository.confirmRefund(
                command.refundRequestId(),
                command.adminNote(),
                Instant.now()
        );
    }

    public String successMessage() {
        return "Xac nhan da hoan tien thanh cong.";
    }
}
