package com.twohands.commerce_service.domain.shipment;

import java.util.List;

public interface GhnAddressMasterDataGateway {

    List<GhnProvince> listProvinces();

    List<GhnDistrict> listDistricts(int provinceId);

    List<GhnWard> listWards(int districtId);
}
