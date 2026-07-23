package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.order.AdminRefundApprovalListSearchCriteria;
import com.twohands.commerce_service.domain.payment.PaymentRefundRequestStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminRefundApprovalRepository {

    long countAdminRefundApprovals(AdminRefundApprovalListSearchCriteria criteria);

    List<AdminRefundApprovalItem> findAdminRefundApprovals(
            AdminRefundApprovalListSearchCriteria criteria,
            PageQuery pageQuery
    );

    Optional<AdminRefundApprovalItem> findById(UUID refundRequestId);

    AdminRefundApprovalItem confirmRefund(UUID refundRequestId, String adminNote, Instant now);

    AdminRefundApprovalItem rejectRefund(UUID refundRequestId, String adminNote, Instant now);
}
