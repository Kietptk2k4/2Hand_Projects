package com.twohands.commerce_service.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "seller_shipping_profiles")
public class SellerShippingProfileEntity {

    @Id
    @Column(name = "shop_id", nullable = false, updatable = false)
    private UUID shopId;

    @Column(name = "province_code", nullable = false)
    private String provinceCode;

    @Column(name = "district_code", nullable = false)
    private String districtCode;

    @Column(name = "ward_code", nullable = false)
    private String wardCode;

    public UUID getShopId() {
        return shopId;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public String getWardCode() {
        return wardCode;
    }
}
