package com.twohands.commerce_service.application.finance.admin.viewadminsellerledger;

import com.twohands.commerce_service.application.finance.viewsellerledger.ViewSellerLedgerUseCase;
import com.twohands.commerce_service.application.finance.viewsellerledger.ViewSellerLedgerCommand;
import com.twohands.commerce_service.domain.finance.ViewSellerLedgerResult;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewAdminSellerLedgerUseCase {

    private final SellerShopRepository sellerShopRepository;
    private final ViewSellerLedgerUseCase viewSellerLedgerUseCase;

    public ViewAdminSellerLedgerUseCase(
            SellerShopRepository sellerShopRepository,
            ViewSellerLedgerUseCase viewSellerLedgerUseCase
    ) {
        this.sellerShopRepository = sellerShopRepository;
        this.viewSellerLedgerUseCase = viewSellerLedgerUseCase;
    }

    @Transactional(readOnly = true)
    public ViewSellerLedgerResult execute(ViewAdminSellerLedgerCommand command) {
        sellerShopRepository.findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_SHOP_NOT_FOUND));

        return viewSellerLedgerUseCase.execute(
                new ViewSellerLedgerCommand(command.sellerId(), command.page(), command.limit())
        );
    }

    public String successMessage() {
        return "Lay so cai seller thanh cong.";
    }
}
