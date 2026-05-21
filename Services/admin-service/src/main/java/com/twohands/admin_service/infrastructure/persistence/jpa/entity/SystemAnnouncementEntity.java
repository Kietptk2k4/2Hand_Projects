package com.twohands.admin_service.infrastructure.persistence.jpa.entity;

import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AnnouncementSeverity;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AnnouncementStatus;
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
@Table(name = "system_announcements")
public class SystemAnnouncementEntity {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "content", nullable = false)
	private String content;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "severity", nullable = false, columnDefinition = "announcement_severity")
	private AnnouncementSeverity severity;

	@Column(name = "is_pinned", nullable = false)
	private boolean pinned;

	@Column(name = "dismissible", nullable = false)
	private boolean dismissible = true;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "status", nullable = false, columnDefinition = "announcement_status")
	private AnnouncementStatus status = AnnouncementStatus.DRAFT;

	@Column(name = "created_by", nullable = false)
	private UUID createdBy;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "sent_at")
	private Instant sentAt;

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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public AnnouncementSeverity getSeverity() {
		return severity;
	}

	public void setSeverity(AnnouncementSeverity severity) {
		this.severity = severity;
	}

	public boolean isPinned() {
		return pinned;
	}

	public void setPinned(boolean pinned) {
		this.pinned = pinned;
	}

	public boolean isDismissible() {
		return dismissible;
	}

	public void setDismissible(boolean dismissible) {
		this.dismissible = dismissible;
	}

	public AnnouncementStatus getStatus() {
		return status;
	}

	public void setStatus(AnnouncementStatus status) {
		this.status = status;
	}

	public UUID getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(UUID createdBy) {
		this.createdBy = createdBy;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getSentAt() {
		return sentAt;
	}

	public void setSentAt(Instant sentAt) {
		this.sentAt = sentAt;
	}
}
