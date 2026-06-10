package com.twohands.commerce_service.application.finance.payout.createsellerpayoutaccount;

import com.twohands.commerce_service.domain.finance.SellerPayoutAccount;
import com.twohands.commerce_service.domain.finance.SellerPayoutRepository;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class CreateSellerPayoutAccountUseCase {

    private final SellerShopRepository sellerShopRepository;
    private final SellerPayoutRepository sellerPayoutRepository;

    public CreateSellerPayoutAccountUseCase(
            SellerShopRepository sellerShopRepository,
            SellerPayoutRepository sellerPayoutRepository
    ) {
        this.sellerShopRepository = sellerShopRepository;
        this.sellerPayoutRepository = sellerPayoutRepository;
    }

    @Transactional
    public SellerPayoutAccount execute(CreateSellerPayoutAccountCommand command) {
        sellerShopRepository.findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_SHOP_NOT_FOUND));

        Instant now = Instant.now();
        if (command.isDefault()) {
            sellerPayoutRepository.clearDefaultAccounts(command.sellerId(), now);
        }

        UUID accountId = sellerPayoutRepository.createAccount(
                command.sellerId(),
                command.bankName().trim(),
                command.bankAccountName().trim(),
                command.bankAccountNumber().trim(),
                command.isDefault(),
                now
        );

        return sellerPayoutRepository.findAccountById(command.sellerId(), accountId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_ACCOUNT_NOT_FOUND));
    }

    public String successMessage() {
        return "Tao tai khoan rut tien thanh cong.";
    }
}
