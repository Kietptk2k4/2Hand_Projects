package com.twohands.authservice.infrastructure.persistence.mapper;

import com.twohands.authservice.domain.user.UserSettings;
import com.twohands.authservice.infrastructure.persistence.entity.UserEntity;
import com.twohands.authservice.infrastructure.persistence.entity.UserSettingsEntity;
import org.springframework.stereotype.Component;

@Component
public class UserSettingsMapper {

    public UserSettings toDomain(UserSettingsEntity entity) {
        if (entity == null) {
            return null;
        }
        UserSettings domain = new UserSettings();
        domain.setUserId(entity.getUserId());
        domain.setAppearanceMode(entity.getAppearanceMode());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }

    public UserSettingsEntity toEntity(UserSettings domain) {
        if (domain == null) {
            return null;
        }
        UserSettingsEntity entity = new UserSettingsEntity();
        entity.setUserId(domain.getUserId());
        entity.setAppearanceMode(domain.getAppearanceMode());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        if (domain.getUserId() != null) {
            UserEntity user = new UserEntity();
            user.setId(domain.getUserId());
            entity.setUser(user);
        }
        return entity;
    }
}
