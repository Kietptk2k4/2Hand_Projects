package com.twohands.authservice.infrastructure.persistence.adapter;

import com.twohands.authservice.domain.role.Role;
import com.twohands.authservice.domain.role.RoleRepository;
import com.twohands.authservice.infrastructure.persistence.mapper.RoleMapper;
import com.twohands.authservice.infrastructure.persistence.repository.RoleJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RoleRepositoryAdapter implements RoleRepository {

    private final RoleJpaRepository jpaRepository;
    private final RoleMapper roleMapper;

    public RoleRepositoryAdapter(RoleJpaRepository jpaRepository, RoleMapper roleMapper) {
        this.jpaRepository = jpaRepository;
        this.roleMapper = roleMapper;
    }

    @Override
    public Optional<Role> findByCode(String code) {
        return jpaRepository.findByCode(code).map(roleMapper::toDomain);
    }
}
