package com.twohands.commerce_service.application.finance.payout.listsellerpayoutrequests;

import com.twohands.commerce_service.application.finance.payout.SellerPayoutPageSupport;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.finance.SellerPayoutRepository;
import com.twohands.commerce_service.domain.finance.SellerPayoutRequest;
import com.twohands.commerce_service.domain.finance.ViewSellerPayoutRequestsResult;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListSellerPayoutRequestsUseCase {

    private final SellerShopRepository sellerShopRepository;
    private final SellerPayoutRepository sellerPayoutRepository;

    public ListSellerPayoutRequestsUseCase(
            SellerShopRepository sellerShopRepository,
            SellerPayoutRepository sellerPayoutRepository
    ) {
        this.sellerShopRepository = sellerShopRepository;
        this.sellerPayoutRepository = sellerPayoutRepository;
    }

    @Transactional(readOnly = true)
    public ViewSellerPayoutRequestsResult execute(ListSellerPayoutRequestsCommand command) {
        sellerShopRepository.findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_SHOP_NOT_FOUND));

        PageQuery pageQuery = SellerPayoutPageSupport.resolvePageQuery(command.page(), command.limit());
        long totalItems = sellerPayoutRepository.countPayoutRequests(command.sellerId(), command.status());
        List<SellerPayoutRequest> items = totalItems == 0
                ? List.of()
                : sellerPayoutRepository.findPayoutRequestsBySellerId(command.sellerId(), command.status(), pageQuery);

        return new ViewSellerPayoutRequestsResult(items, PageMeta.of(pageQuery.page(), pageQuery.limit(), totalItems));
    }

    public String successMessage() {
        return "Lay danh sach yeu cau rut tien thanh cong.";
    }
}
