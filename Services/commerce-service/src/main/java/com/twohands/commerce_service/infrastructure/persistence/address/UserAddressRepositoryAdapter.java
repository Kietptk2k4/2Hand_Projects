package com.twohands.commerce_service.infrastructure.persistence.address;

import com.twohands.commerce_service.domain.address.UserAddress;
import com.twohands.commerce_service.domain.address.UserAddressRepository;
import com.twohands.commerce_service.infrastructure.persistence.jpa.repository.UserAddressJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserAddressRepositoryAdapter implements UserAddressRepository {

    private final UserAddressJpaRepository userAddressJpaRepository;

    public UserAddressRepositoryAdapter(UserAddressJpaRepository userAddressJpaRepository) {
        this.userAddressJpaRepository = userAddressJpaRepository;
    }

    @Override
    public Optional<UserAddress> findByIdAndUserId(UUID addressId, UUID userId) {
        return userAddressJpaRepository.findByIdAndUserId(addressId, userId)
                .map(entity -> new UserAddress(
                        entity.getId(),
                        entity.getUserId(),
                        entity.getProvinceCode(),
                        entity.getDistrictCode(),
                        entity.getWardCode()
                ));
    }
}
