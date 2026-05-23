package com.twohands.admin_service.infrastructure.persistence.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.domain.audit.AdminActionLog;
import com.twohands.admin_service.domain.audit.AdminActionLogRepository;
import com.twohands.admin_service.domain.audit.AdminActionLogSearchCriteria;
import com.twohands.admin_service.domain.audit.AdminActionStatus;
import com.twohands.admin_service.domain.common.PageRequest;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.infrastructure.persistence.jpa.entity.AdminActionLogEntity;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.AdminActionLogJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class AdminActionLogRepositoryAdapter implements AdminActionLogRepository {

	private final AdminActionLogJpaRepository adminActionLogJpaRepository;
	private final AdminActionLogSearchRepository adminActionLogSearchRepository;
	private final ObjectMapper objectMapper;

	public AdminActionLogRepositoryAdapter(
			AdminActionLogJpaRepository adminActionLogJpaRepository,
			AdminActionLogSearchRepository adminActionLogSearchRepository,
			ObjectMapper objectMapper
	) {
		this.adminActionLogJpaRepository = adminActionLogJpaRepository;
		this.adminActionLogSearchRepository = adminActionLogSearchRepository;
		this.objectMapper = objectMapper;
	}

	@Override
	public Optional<AdminActionLog> findById(UUID logId) {
		return adminActionLogJpaRepository.findById(logId).map(this::toDomain);
	}

	@Override
	public PagedResult<AdminActionLog> search(AdminActionLogSearchCriteria criteria, PageRequest pageRequest) {
		PagedResult<AdminActionLogEntity> page = adminActionLogSearchRepository.search(criteria, pageRequest);
		return new PagedResult<>(
				page.items().stream().map(this::toDomain).toList(),
				page.page(),
				page.size(),
				page.totalElements(),
				page.totalPages()
		);
	}

	@Override
	public AdminActionLog save(AdminActionLog log) {
		AdminActionLogEntity entity = toEntity(log);
		AdminActionLogEntity saved = adminActionLogJpaRepository.save(entity);
		return toDomain(saved);
	}

	private AdminActionLogEntity toEntity(AdminActionLog log) {
		AdminActionLogEntity entity = new AdminActionLogEntity();
		entity.setId(log.id());
		entity.setAdminId(log.adminId());
		entity.setActionType(AdminActionType.valueOf(log.actionType()));
		entity.setTargetType(log.targetType());
		entity.setTargetId(log.targetId());
		entity.setRequestPayload(log.requestPayloadJson());
		entity.setResponsePayload(log.responsePayloadJson());
		entity.setIpAddress(log.ipAddress());
		entity.setUserAgent(log.userAgent());
		entity.setCreatedAt(log.createdAt());
		return entity;
	}

	private AdminActionLog toDomain(AdminActionLogEntity entity) {
		return new AdminActionLog(
				entity.getId(),
				entity.getAdminId(),
				entity.getActionType().name(),
				entity.getTargetType(),
				entity.getTargetId(),
				parseStatus(entity.getResponsePayload()),
				entity.getRequestPayload(),
				entity.getResponsePayload(),
				entity.getIpAddress(),
				entity.getUserAgent(),
				entity.getCreatedAt()
		);
	}

	private AdminActionStatus parseStatus(String responsePayload) {
		if (responsePayload == null || responsePayload.isBlank()) {
			return AdminActionStatus.SUCCESS;
		}
		try {
			JsonNode node = objectMapper.readTree(responsePayload);
			String status = node.path("status").asText("SUCCESS");
			return AdminActionStatus.valueOf(status);
		} catch (Exception ex) {
			return AdminActionStatus.SUCCESS;
		}
	}
}
