package com.twohands.authservice.infrastructure.persistence.mapper;

import com.twohands.authservice.domain.permission.Permission;
import com.twohands.authservice.infrastructure.persistence.entity.PermissionEntity;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public Permission toDomain(PermissionEntity entity) {
        if (entity == null) {
            return null;
        }
        Permission domain = new Permission();
        domain.setId(entity.getId());
        domain.setCode(entity.getCode());
        domain.setDescription(entity.getDescription());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }

    public PermissionEntity toEntity(Permission domain) {
        if (domain == null) {
            return null;
        }
        PermissionEntity entity = new PermissionEntity();
        entity.setId(domain.getId());
        entity.setCode(domain.getCode());
        entity.setDescription(domain.getDescription());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
