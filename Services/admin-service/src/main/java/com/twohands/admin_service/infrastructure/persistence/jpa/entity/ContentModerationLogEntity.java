package com.twohands.admin_service.infrastructure.persistence.jpa.entity;

import com.twohands.admin_service.infrastructure.persistence.jpa.enums.ContentModerationAction;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.ContentModerationTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "content_moderation_logs")
public class ContentModerationLogEntity {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "target_type", nullable = false, columnDefinition = "content_moderation_target_type")
	private ContentModerationTargetType targetType;

	@Column(name = "target_id")
	private String targetId;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "action", nullable = false, columnDefinition = "content_moderation_action")
	private ContentModerationAction action;

	@Column(name = "reason", nullable = false)
	private String reason;

	@Column(name = "admin_id", nullable = false)
	private UUID adminId;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "note")
	private String note;

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

	public ContentModerationTargetType getTargetType() {
		return targetType;
	}

	public void setTargetType(ContentModerationTargetType targetType) {
		this.targetType = targetType;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public ContentModerationAction getAction() {
		return action;
	}

	public void setAction(ContentModerationAction action) {
		this.action = action;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public UUID getAdminId() {
		return adminId;
	}

	public void setAdminId(UUID adminId) {
		this.adminId = adminId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}
