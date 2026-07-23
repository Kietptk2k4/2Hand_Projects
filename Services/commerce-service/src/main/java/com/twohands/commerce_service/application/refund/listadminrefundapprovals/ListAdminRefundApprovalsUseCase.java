package com.twohands.commerce_service.application.refund.listadminrefundapprovals;

import com.twohands.commerce_service.application.finance.payout.SellerPayoutPageSupport;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.order.AdminRefundApprovalItem;
import com.twohands.commerce_service.domain.order.AdminRefundApprovalListSearchCriteria;
import com.twohands.commerce_service.domain.order.AdminRefundApprovalRepository;
import com.twohands.commerce_service.domain.order.ViewAdminRefundApprovalsResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListAdminRefundApprovalsUseCase {

    private final AdminRefundApprovalRepository adminRefundApprovalRepository;

    public ListAdminRefundApprovalsUseCase(AdminRefundApprovalRepository adminRefundApprovalRepository) {
        this.adminRefundApprovalRepository = adminRefundApprovalRepository;
    }

    @Transactional(readOnly = true)
    public ViewAdminRefundApprovalsResult execute(ListAdminRefundApprovalsCommand command) {
        AdminRefundApprovalListSearchCriteria criteria = AdminRefundApprovalListQueryPolicy.toCriteria(
                command.status().map(Enum::name).orElse(null),
                command.searchQuery().orElse(null),
                command.requestedBy().orElse(null),
                command.paymentMethod().orElse(null),
                command.from().orElse(null),
                command.to().orElse(null)
        );
        PageQuery pageQuery = SellerPayoutPageSupport.resolvePageQuery(command.page(), command.limit());
        long totalItems = adminRefundApprovalRepository.countAdminRefundApprovals(criteria);
        List<AdminRefundApprovalItem> items = totalItems == 0
                ? List.of()
                : adminRefundApprovalRepository.findAdminRefundApprovals(criteria, pageQuery);

        return new ViewAdminRefundApprovalsResult(
                items,
                PageMeta.of(pageQuery.page(), pageQuery.limit(), totalItems)
        );
    }

    public String successMessage() {
        return "Lay danh sach duyet hoan tien thanh cong.";
    }
}
