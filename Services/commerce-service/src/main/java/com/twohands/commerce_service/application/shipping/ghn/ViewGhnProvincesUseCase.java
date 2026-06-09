package com.twohands.commerce_service.application.shipping.ghn;

import com.twohands.commerce_service.domain.shipment.GhnAddressMasterDataGateway;
import com.twohands.commerce_service.domain.shipment.GhnProvince;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ViewGhnProvincesUseCase {

    private final GhnAddressMasterDataGateway ghnAddressMasterDataGateway;

    public ViewGhnProvincesUseCase(GhnAddressMasterDataGateway ghnAddressMasterDataGateway) {
        this.ghnAddressMasterDataGateway = ghnAddressMasterDataGateway;
    }

    public List<GhnProvince> execute() {
        return ghnAddressMasterDataGateway.listProvinces();
    }

    public String successMessage() {
        return "Lay danh sach tinh/thanh GHN thanh cong.";
    }
}
