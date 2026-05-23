package com.twohands.admin_service.infrastructure.persistence.jpa.repository;

import com.twohands.admin_service.infrastructure.persistence.jpa.entity.UserEnforcementLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface UserEnforcementLogJpaRepository extends JpaRepository<UserEnforcementLogEntity, UUID> {

	@Query("""
			SELECT l FROM UserEnforcementLogEntity l
			WHERE l.enforcement.id IN :enforcementIds
			ORDER BY l.createdAt DESC
			""")
	List<UserEnforcementLogEntity> findByEnforcementIdsOrderByCreatedAtDesc(
			@Param("enforcementIds") Collection<UUID> enforcementIds
	);
}
