package com.twohands.authservice.infrastructure.persistence.mapper;

import com.twohands.authservice.domain.user.UserProfile;
import com.twohands.authservice.infrastructure.persistence.entity.UserEntity;
import com.twohands.authservice.infrastructure.persistence.entity.UserProfileEntity;
import org.springframework.stereotype.Component;

@Component
public class UserProfileMapper {

    public UserProfile toDomain(UserProfileEntity entity) {
        if (entity == null) {
            return null;
        }
        UserProfile domain = new UserProfile();
        domain.setUserId(entity.getUserId());
        domain.setDisplayName(entity.getDisplayName());
        domain.setAvatarUrl(entity.getAvatarUrl());
        domain.setBio(entity.getBio());
        domain.setWebsite(entity.getWebsite());
        domain.setSocialLink(entity.getSocialLink());
        domain.setPrivate(entity.isPrivate());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }

    public UserProfileEntity toEntity(UserProfile domain) {
        if (domain == null) {
            return null;
        }
        UserProfileEntity entity = new UserProfileEntity();
        entity.setUserId(domain.getUserId());
        entity.setDisplayName(domain.getDisplayName());
        entity.setAvatarUrl(domain.getAvatarUrl());
        entity.setBio(domain.getBio());
        entity.setWebsite(domain.getWebsite());
        entity.setSocialLink(domain.getSocialLink());
        entity.setPrivate(domain.isPrivate());
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
