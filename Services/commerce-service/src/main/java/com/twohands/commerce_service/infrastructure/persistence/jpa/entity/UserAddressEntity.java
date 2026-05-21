package com.twohands.commerce_service.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "user_addresses")
public class UserAddressEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "province_code", nullable = false)
    private String provinceCode;

    @Column(name = "district_code", nullable = false)
    private String districtCode;

    @Column(name = "ward_code", nullable = false)
    private String wardCode;

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
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
