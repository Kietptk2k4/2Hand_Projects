package com.twohands.commerce_service.delivery.http.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.shipment.GhnDistrict;
import com.twohands.commerce_service.domain.shipment.GhnProvince;
import com.twohands.commerce_service.domain.shipment.GhnWard;

import java.util.List;

public final class GhnAddressMasterDataResponses {

    private GhnAddressMasterDataResponses() {
    }

    public record GhnProvinceResponse(
            @JsonProperty("province_id") int provinceId,
            @JsonProperty("province_name") String provinceName,
            @JsonProperty("code") String code
    ) {
        public static GhnProvinceResponse from(GhnProvince province) {
            return new GhnProvinceResponse(province.provinceId(), province.provinceName(), province.code());
        }
    }

    public record ViewGhnProvincesResponse(
            @JsonProperty("provinces") List<GhnProvinceResponse> provinces
    ) {
        public static ViewGhnProvincesResponse from(List<GhnProvince> provinces) {
            return new ViewGhnProvincesResponse(provinces.stream().map(GhnProvinceResponse::from).toList());
        }
    }

    public record GhnDistrictResponse(
            @JsonProperty("district_id") int districtId,
            @JsonProperty("province_id") int provinceId,
            @JsonProperty("district_name") String districtName,
            @JsonProperty("code") String code
    ) {
        public static GhnDistrictResponse from(GhnDistrict district) {
            return new GhnDistrictResponse(
                    district.districtId(),
                    district.provinceId(),
                    district.districtName(),
                    district.code()
            );
        }
    }

    public record ViewGhnDistrictsResponse(
            @JsonProperty("districts") List<GhnDistrictResponse> districts
    ) {
        public static ViewGhnDistrictsResponse from(List<GhnDistrict> districts) {
            return new ViewGhnDistrictsResponse(districts.stream().map(GhnDistrictResponse::from).toList());
        }
    }

    public record GhnWardResponse(
            @JsonProperty("ward_code") String wardCode,
            @JsonProperty("district_id") int districtId,
            @JsonProperty("ward_name") String wardName
    ) {
        public static GhnWardResponse from(GhnWard ward) {
            return new GhnWardResponse(ward.wardCode(), ward.districtId(), ward.wardName());
        }
    }

    public record ViewGhnWardsResponse(
            @JsonProperty("wards") List<GhnWardResponse> wards
    ) {
        public static ViewGhnWardsResponse from(List<GhnWard> wards) {
            return new ViewGhnWardsResponse(wards.stream().map(GhnWardResponse::from).toList());
        }
    }
}
