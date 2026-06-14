package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.order.AdminRefundApprovalItem;
import com.twohands.commerce_service.domain.order.ViewAdminRefundApprovalsResult;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.delivery.http.catalog.PageMetaResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminRefundApprovalResponse(
        @JsonProperty("id") UUID id,
        @JsonProperty("payment_id") UUID paymentId,
        @JsonProperty("order_id") UUID orderId,
        @JsonProperty("buyer_id") UUID buyerId,
        @JsonProperty("requested_by") String requestedBy,
        @JsonProperty("requested_by_user_id") UUID requestedByUserId,
        @JsonProperty("status") String status,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("reason") String reason,
        @JsonProperty("admin_note") String adminNote,
        @JsonProperty("payment_method") String paymentMethod,
        @JsonProperty("order_payment_status") String orderPaymentStatus,
        @JsonProperty("order_status") String orderStatus,
        @JsonProperty("requested_at") Instant requestedAt,
        @JsonProperty("confirmed_at") Instant confirmedAt,
        @JsonProperty("rejected_at") Instant rejectedAt
) {
    public static AdminRefundApprovalResponse from(AdminRefundApprovalItem item) {
        return new AdminRefundApprovalResponse(
                item.id(),
                item.paymentId(),
                item.orderId(),
                item.buyerId(),
                item.requestedBy().name(),
                item.requestedByUserId(),
                item.status().name(),
                item.amount(),
                item.reason(),
                item.adminNote(),
                item.paymentMethod().name(),
                item.orderPaymentStatus().name(),
                item.orderStatus().name(),
                item.requestedAt(),
                item.confirmedAt(),
                item.rejectedAt()
        );
    }
}

record ViewAdminRefundApprovalsResponse(
        @JsonProperty("items") List<AdminRefundApprovalResponse> items,
        @JsonProperty("pagination") PageMetaResponse pagination
) {
    static ViewAdminRefundApprovalsResponse from(ViewAdminRefundApprovalsResult result) {
        PageMeta pagination = result.pagination();
        return new ViewAdminRefundApprovalsResponse(
                result.items().stream().map(AdminRefundApprovalResponse::from).toList(),
                new PageMetaResponse(
                        pagination.page(),
                        pagination.limit(),
                        pagination.totalItems(),
                        pagination.totalPages(),
                        pagination.hasNext()
                )
        );
    }
}

record RejectRefundApprovalBody(@JsonProperty("admin_note") String adminNote) {
}

record ConfirmRefundApprovalBody(@JsonProperty("admin_note") String adminNote) {
}
