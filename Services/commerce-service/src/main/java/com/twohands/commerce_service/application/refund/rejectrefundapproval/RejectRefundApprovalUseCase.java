package com.twohands.commerce_service.application.refund.rejectrefundapproval;

import com.twohands.commerce_service.domain.order.AdminRefundApprovalItem;
import com.twohands.commerce_service.domain.order.AdminRefundApprovalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RejectRefundApprovalUseCase {

    private final AdminRefundApprovalRepository adminRefundApprovalRepository;

    public RejectRefundApprovalUseCase(AdminRefundApprovalRepository adminRefundApprovalRepository) {
        this.adminRefundApprovalRepository = adminRefundApprovalRepository;
    }

    @Transactional
    public AdminRefundApprovalItem execute(RejectRefundApprovalCommand command) {
        return adminRefundApprovalRepository.rejectRefund(
                command.refundRequestId(),
                command.adminNote(),
                Instant.now()
        );
    }

    public String successMessage() {
        return "Tu choi yeu cau hoan tien thanh cong.";
    }
}
