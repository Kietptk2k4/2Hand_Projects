package com.twohands.commerce_service.application.finance.payout.listadminpayoutrequests;

import com.twohands.commerce_service.application.finance.payout.SellerPayoutPageSupport;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.finance.SellerPayoutRepository;
import com.twohands.commerce_service.domain.finance.SellerPayoutRequest;
import com.twohands.commerce_service.domain.finance.ViewSellerPayoutRequestsResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListAdminPayoutRequestsUseCase {

    private final SellerPayoutRepository sellerPayoutRepository;

    public ListAdminPayoutRequestsUseCase(SellerPayoutRepository sellerPayoutRepository) {
        this.sellerPayoutRepository = sellerPayoutRepository;
    }

    @Transactional(readOnly = true)
    public ViewSellerPayoutRequestsResult execute(ListAdminPayoutRequestsCommand command) {
        PageQuery pageQuery = SellerPayoutPageSupport.resolvePageQuery(command.page(), command.limit());
        long totalItems = sellerPayoutRepository.countAdminPayoutRequests(command.status());
        List<SellerPayoutRequest> items = totalItems == 0
                ? List.of()
                : sellerPayoutRepository.findAdminPayoutRequests(command.status(), pageQuery);

        return new ViewSellerPayoutRequestsResult(items, PageMeta.of(pageQuery.page(), pageQuery.limit(), totalItems));
    }

    public String successMessage() {
        return "Lay hang doi rut tien thanh cong.";
    }
}
