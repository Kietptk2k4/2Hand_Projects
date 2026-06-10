package com.twohands.commerce_service.application.finance.viewsellerledger;

import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.finance.SellerLedgerListEntry;
import com.twohands.commerce_service.domain.finance.SellerLedgerRepository;
import com.twohands.commerce_service.domain.finance.ViewSellerLedgerResult;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ViewSellerLedgerUseCase {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    private final SellerShopRepository sellerShopRepository;
    private final SellerLedgerRepository sellerLedgerRepository;

    public ViewSellerLedgerUseCase(
            SellerShopRepository sellerShopRepository,
            SellerLedgerRepository sellerLedgerRepository
    ) {
        this.sellerShopRepository = sellerShopRepository;
        this.sellerLedgerRepository = sellerLedgerRepository;
    }

    @Transactional(readOnly = true)
    public ViewSellerLedgerResult execute(ViewSellerLedgerCommand command) {
        sellerShopRepository.findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_SHOP_NOT_FOUND));

        PageQuery pageQuery = resolvePageQuery(command.page(), command.limit());
        long totalItems = sellerLedgerRepository.countLedgerEntries(command.sellerId());
        List<SellerLedgerListEntry> items = totalItems == 0
                ? List.of()
                : sellerLedgerRepository.findLedgerEntries(command.sellerId(), pageQuery);

        return new ViewSellerLedgerResult(items, PageMeta.of(pageQuery.page(), pageQuery.limit(), totalItems));
    }

    public String successMessage() {
        return "Lay lich su so cai seller thanh cong.";
    }

    private PageQuery resolvePageQuery(Integer page, Integer limit) {
        int resolvedPage = page == null ? DEFAULT_PAGE : page;
        int resolvedLimit = limit == null ? DEFAULT_LIMIT : limit;
        if (resolvedPage < 1) {
            throw new AppException(ErrorCode.INVALID_PAGINATION, "page must be >= 1", "page", "must be >= 1");
        }
        if (resolvedLimit < 1 || resolvedLimit > MAX_LIMIT) {
            throw new AppException(
                    ErrorCode.INVALID_PAGINATION,
                    "limit must be between 1 and " + MAX_LIMIT,
                    "limit",
                    "must be between 1 and " + MAX_LIMIT
            );
        }
        return new PageQuery(resolvedPage, resolvedLimit);
    }
}
