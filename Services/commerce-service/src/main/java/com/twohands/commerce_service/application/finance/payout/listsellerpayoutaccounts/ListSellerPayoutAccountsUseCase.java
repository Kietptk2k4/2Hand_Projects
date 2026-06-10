package com.twohands.commerce_service.application.finance.payout.listsellerpayoutaccounts;

import com.twohands.commerce_service.domain.finance.SellerPayoutRepository;
import com.twohands.commerce_service.domain.finance.ViewSellerPayoutAccountsResult;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListSellerPayoutAccountsUseCase {

    private final SellerShopRepository sellerShopRepository;
    private final SellerPayoutRepository sellerPayoutRepository;

    public ListSellerPayoutAccountsUseCase(
            SellerShopRepository sellerShopRepository,
            SellerPayoutRepository sellerPayoutRepository
    ) {
        this.sellerShopRepository = sellerShopRepository;
        this.sellerPayoutRepository = sellerPayoutRepository;
    }

    @Transactional(readOnly = true)
    public ViewSellerPayoutAccountsResult execute(ListSellerPayoutAccountsCommand command) {
        sellerShopRepository.findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_SHOP_NOT_FOUND));
        return new ViewSellerPayoutAccountsResult(
                sellerPayoutRepository.findAccountsBySellerId(command.sellerId())
        );
    }

    public String successMessage() {
        return "Lay danh sach tai khoan rut tien thanh cong.";
    }
}
