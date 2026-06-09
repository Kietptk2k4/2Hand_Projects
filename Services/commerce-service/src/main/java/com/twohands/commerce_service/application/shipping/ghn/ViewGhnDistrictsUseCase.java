package com.twohands.commerce_service.application.shipping.ghn;

import com.twohands.commerce_service.domain.shipment.GhnAddressMasterDataGateway;
import com.twohands.commerce_service.domain.shipment.GhnDistrict;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ViewGhnDistrictsUseCase {

    private final GhnAddressMasterDataGateway ghnAddressMasterDataGateway;

    public ViewGhnDistrictsUseCase(GhnAddressMasterDataGateway ghnAddressMasterDataGateway) {
        this.ghnAddressMasterDataGateway = ghnAddressMasterDataGateway;
    }

    public List<GhnDistrict> execute(int provinceId) {
        if (provinceId <= 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "province_id must be positive");
        }
        return ghnAddressMasterDataGateway.listDistricts(provinceId);
    }

    public String successMessage() {
        return "Lay danh sach quan/huyen GHN thanh cong.";
    }
}
