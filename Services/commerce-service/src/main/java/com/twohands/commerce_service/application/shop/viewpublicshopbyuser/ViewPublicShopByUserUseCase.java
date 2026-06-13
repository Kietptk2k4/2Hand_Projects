package com.twohands.commerce_service.application.shop.viewpublicshopbyuser;

import com.twohands.commerce_service.domain.shop.PublicShopByUserSnapshot;
import com.twohands.commerce_service.domain.shop.ViewPublicShopByUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewPublicShopByUserUseCase {

    private final ViewPublicShopByUserRepository viewPublicShopByUserRepository;

    public ViewPublicShopByUserUseCase(ViewPublicShopByUserRepository viewPublicShopByUserRepository) {
        this.viewPublicShopByUserRepository = viewPublicShopByUserRepository;
    }

    @Transactional(readOnly = true)
    public PublicShopByUserSnapshot execute(ViewPublicShopByUserCommand command) {
        return viewPublicShopByUserRepository
                .findActiveShopBySellerId(command.userId())
                .orElseGet(PublicShopByUserSnapshot::none);
    }

    public String successMessage() {
        return "Lay thong tin shop thanh cong.";
    }
}
