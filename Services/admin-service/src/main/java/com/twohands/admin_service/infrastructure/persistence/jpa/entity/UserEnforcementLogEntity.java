package com.twohands.admin_service.infrastructure.persistence.jpa.entity;

import com.twohands.admin_service.infrastructure.persistence.jpa.enums.UserEnforcementStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_enforcement_logs")
public class UserEnforcementLogEntity {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "enforcement_id", nullable = false)
	private UserEnforcementEntity enforcement;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "old_status", columnDefinition = "user_enforcement_status")
	private UserEnforcementStatus oldStatus;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "new_status", nullable = false, columnDefinition = "user_enforcement_status")
	private UserEnforcementStatus newStatus;

	@Column(name = "admin_id")
	private UUID adminId;

	@Column(name = "note")
	private String note;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UserEnforcementEntity getEnforcement() {
		return enforcement;
	}

	public void setEnforcement(UserEnforcementEntity enforcement) {
		this.enforcement = enforcement;
	}

	public UserEnforcementStatus getOldStatus() {
		return oldStatus;
	}

	public void setOldStatus(UserEnforcementStatus oldStatus) {
		this.oldStatus = oldStatus;
	}

	public UserEnforcementStatus getNewStatus() {
		return newStatus;
	}

	public void setNewStatus(UserEnforcementStatus newStatus) {
		this.newStatus = newStatus;
	}

	public UUID getAdminId() {
		return adminId;
	}

	public void setAdminId(UUID adminId) {
		this.adminId = adminId;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
