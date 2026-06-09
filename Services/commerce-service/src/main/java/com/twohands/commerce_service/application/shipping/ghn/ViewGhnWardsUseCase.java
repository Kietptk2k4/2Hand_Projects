package com.twohands.commerce_service.application.shipping.ghn;

import com.twohands.commerce_service.domain.shipment.GhnAddressMasterDataGateway;
import com.twohands.commerce_service.domain.shipment.GhnWard;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ViewGhnWardsUseCase {

    private final GhnAddressMasterDataGateway ghnAddressMasterDataGateway;

    public ViewGhnWardsUseCase(GhnAddressMasterDataGateway ghnAddressMasterDataGateway) {
        this.ghnAddressMasterDataGateway = ghnAddressMasterDataGateway;
    }

    public List<GhnWard> execute(int districtId) {
        if (districtId <= 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "district_id must be positive");
        }
        return ghnAddressMasterDataGateway.listWards(districtId);
    }

    public String successMessage() {
        return "Lay danh sach phuong/xa GHN thanh cong.";
    }
}
