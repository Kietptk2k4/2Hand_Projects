package com.twohands.admin_service.infrastructure.persistence.jpa.entity;

import com.twohands.admin_service.infrastructure.persistence.jpa.enums.OutboxStatusType;
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
@Table(name = "outbox_events")
public class OutboxEventEntity {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "event_type", nullable = false)
	private String eventType;

	@Column(name = "aggregate_id", nullable = false)
	private UUID aggregateId;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "payload", nullable = false, columnDefinition = "jsonb")
	private String payload;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "status", nullable = false, columnDefinition = "outbox_status")
	private OutboxStatusType status = OutboxStatusType.PENDING;

	@Column(name = "retry_count", nullable = false)
	private int retryCount;

	@Column(name = "last_error")
	private String lastError;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "published_at")
	private Instant publishedAt;

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

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public UUID getAggregateId() {
		return aggregateId;
	}

	public void setAggregateId(UUID aggregateId) {
		this.aggregateId = aggregateId;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public OutboxStatusType getStatus() {
		return status;
	}

	public void setStatus(OutboxStatusType status) {
		this.status = status;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public String getLastError() {
		return lastError;
	}

	public void setLastError(String lastError) {
		this.lastError = lastError;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getPublishedAt() {
		return publishedAt;
	}

	public void setPublishedAt(Instant publishedAt) {
		this.publishedAt = publishedAt;
	}
}
