package com.twohands.admin_service.integration.enforcement;

import com.twohands.admin_service.application.enforcement.expire.ExpireUserEnforcementsUseCase;
import com.twohands.admin_service.infrastructure.persistence.jpa.entity.UserEnforcementEntity;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.UserEnforcementActionType;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.UserEnforcementStatus;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.OutboxEventJpaRepository;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.UserEnforcementJpaRepository;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.UserEnforcementLogJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExpireUserEnforcementsIntegrationTest {

	@Autowired
	private ExpireUserEnforcementsUseCase expireUserEnforcementsUseCase;

	@Autowired
	private UserEnforcementJpaRepository userEnforcementJpaRepository;

	@Autowired
	private UserEnforcementLogJpaRepository userEnforcementLogJpaRepository;

	@Autowired
	private OutboxEventJpaRepository outboxEventJpaRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void execute_expiresActiveEnforcementWithPastExpiresAt() {
		UUID enforcementId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		UUID adminId = UUID.randomUUID();
		Instant now = Instant.now();

		UserEnforcementEntity entity = new UserEnforcementEntity();
		entity.setId(enforcementId);
		entity.setUserId(userId);
		entity.setActionType(UserEnforcementActionType.RESTRICT);
		entity.setReasonCode("SPAM");
		entity.setDescription("Expired restrict test");
		entity.setExpiresAt(now.minusSeconds(120));
		entity.setEnforcedBy(adminId);
		entity.setStatus(UserEnforcementStatus.ACTIVE);
		entity.setCreatedAt(now.minusSeconds(3600));
		entity.setUpdatedAt(now.minusSeconds(3600));
		userEnforcementJpaRepository.save(entity);
		entityManager.flush();

		var firstRun = expireUserEnforcementsUseCase.execute();
		assertEquals(1, firstRun.expiredCount());

		var enforcement = userEnforcementJpaRepository.findById(enforcementId).orElseThrow();
		assertEquals("EXPIRED", enforcement.getStatus().name());

		var logs = userEnforcementLogJpaRepository.findAll().stream()
				.filter(log -> log.getEnforcement().getId().equals(enforcementId))
				.toList();
		assertEquals(1, logs.size());
		assertEquals("EXPIRED", logs.get(0).getNewStatus().name());
		assertNull(logs.get(0).getAdminId());

		var outboxEvents = outboxEventJpaRepository.findAll().stream()
				.filter(event -> "USER_ENFORCEMENT_EXPIRED".equals(event.getEventType()))
				.filter(event -> userId.equals(event.getAggregateId()))
				.toList();
		assertEquals(1, outboxEvents.size());

		var secondRun = expireUserEnforcementsUseCase.execute();
		assertEquals(0, secondRun.expiredCount());
		assertEquals(1, outboxEvents.size());
	}
}
