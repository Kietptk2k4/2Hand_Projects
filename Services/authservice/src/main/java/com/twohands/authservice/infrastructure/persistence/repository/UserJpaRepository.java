package com.twohands.authservice.infrastructure.persistence.repository;

import com.twohands.authservice.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    boolean existsByEmailNormalized(String emailNormalized);

    Optional<UserEntity> findByEmailNormalized(String emailNormalized);
}
