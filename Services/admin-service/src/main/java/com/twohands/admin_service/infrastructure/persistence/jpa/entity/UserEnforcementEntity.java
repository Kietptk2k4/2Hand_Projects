package com.twohands.admin_service.infrastructure.persistence.jpa.entity;

import com.twohands.admin_service.infrastructure.persistence.jpa.enums.UserEnforcementActionType;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.UserEnforcementStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_enforcements")
public class UserEnforcementEntity {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "action_type", nullable = false, columnDefinition = "user_enforcement_action_type")
	private UserEnforcementActionType actionType;

	@Column(name = "reason_code", nullable = false)
	private String reasonCode;

	@Column(name = "description", nullable = false)
	private String description;

	@Column(name = "expires_at")
	private Instant expiresAt;

	@Column(name = "enforced_by", nullable = false)
	private UUID enforcedBy;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "status", nullable = false, columnDefinition = "user_enforcement_status")
	private UserEnforcementStatus status = UserEnforcementStatus.ACTIVE;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	void onCreate() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		Instant now = Instant.now();
		if (createdAt == null) {
			createdAt = now;
		}
		if (updatedAt == null) {
			updatedAt = now;
		}
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public UserEnforcementActionType getActionType() {
		return actionType;
	}

	public void setActionType(UserEnforcementActionType actionType) {
		this.actionType = actionType;
	}

	public String getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}

	public UUID getEnforcedBy() {
		return enforcedBy;
	}

	public void setEnforcedBy(UUID enforcedBy) {
		this.enforcedBy = enforcedBy;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public UserEnforcementStatus getStatus() {
		return status;
	}

	public void setStatus(UserEnforcementStatus status) {
		this.status = status;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
