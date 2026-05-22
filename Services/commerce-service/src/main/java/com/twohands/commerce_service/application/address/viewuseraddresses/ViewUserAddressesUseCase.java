package com.twohands.commerce_service.application.address.viewuseraddresses;

import com.twohands.commerce_service.domain.address.ViewUserAddressesRepository;
import com.twohands.commerce_service.domain.address.ViewUserAddressesResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewUserAddressesUseCase {

    private final ViewUserAddressesRepository viewUserAddressesRepository;

    public ViewUserAddressesUseCase(ViewUserAddressesRepository viewUserAddressesRepository) {
        this.viewUserAddressesRepository = viewUserAddressesRepository;
    }

    @Transactional(readOnly = true)
    public ViewUserAddressesResult execute(ViewUserAddressesCommand command) {
        return viewUserAddressesRepository.findByUserId(command.userId());
    }

    public String successMessage() {
        return "Lay danh sach dia chi giao hang thanh cong.";
    }
}
