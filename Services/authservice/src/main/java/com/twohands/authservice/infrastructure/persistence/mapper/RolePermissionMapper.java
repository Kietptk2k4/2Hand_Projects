package com.twohands.authservice.infrastructure.persistence.mapper;

import com.twohands.authservice.domain.role.RolePermission;
import com.twohands.authservice.infrastructure.persistence.entity.PermissionEntity;
import com.twohands.authservice.infrastructure.persistence.entity.RoleEntity;
import com.twohands.authservice.infrastructure.persistence.entity.RolePermissionEntity;
import org.springframework.stereotype.Component;

@Component
public class RolePermissionMapper {

    public RolePermission toDomain(RolePermissionEntity entity) {
        if (entity == null) {
            return null;
        }
        RolePermission domain = new RolePermission();
        domain.setId(entity.getId());
        if (entity.getRole() != null) {
            domain.setRoleId(entity.getRole().getId());
        }
        if (entity.getPermission() != null) {
            domain.setPermissionId(entity.getPermission().getId());
        }
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }

    public RolePermissionEntity toEntity(RolePermission domain) {
        if (domain == null) {
            return null;
        }
        RolePermissionEntity entity = new RolePermissionEntity();
        entity.setId(domain.getId());
        if (domain.getRoleId() != null) {
            RoleEntity role = new RoleEntity();
            role.setId(domain.getRoleId());
            entity.setRole(role);
        }
        if (domain.getPermissionId() != null) {
            PermissionEntity permission = new PermissionEntity();
            permission.setId(domain.getPermissionId());
            entity.setPermission(permission);
        }
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
