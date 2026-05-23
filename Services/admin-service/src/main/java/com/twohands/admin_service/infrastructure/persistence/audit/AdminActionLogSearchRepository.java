package com.twohands.admin_service.infrastructure.persistence.audit;

import com.twohands.admin_service.domain.audit.AdminActionLogSearchCriteria;
import com.twohands.admin_service.domain.audit.AdminActionStatus;
import com.twohands.admin_service.domain.common.PageRequest;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.infrastructure.persistence.jpa.entity.AdminActionLogEntity;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AdminActionLogSearchRepository {

	private final EntityManager entityManager;

	public AdminActionLogSearchRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public PagedResult<AdminActionLogEntity> search(AdminActionLogSearchCriteria criteria, PageRequest pageRequest) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();

		CriteriaQuery<AdminActionLogEntity> query = cb.createQuery(AdminActionLogEntity.class);
		Root<AdminActionLogEntity> root = query.from(AdminActionLogEntity.class);
		List<Predicate> predicates = buildPredicates(cb, root, criteria);
		query.where(predicates.toArray(Predicate[]::new));
		query.orderBy(cb.desc(root.get("createdAt")));

		TypedQuery<AdminActionLogEntity> typedQuery = entityManager.createQuery(query);
		typedQuery.setFirstResult((pageRequest.page() - 1) * pageRequest.size());
		typedQuery.setMaxResults(pageRequest.size());
		List<AdminActionLogEntity> items = typedQuery.getResultList();

		CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
		Root<AdminActionLogEntity> countRoot = countQuery.from(AdminActionLogEntity.class);
		countQuery.select(cb.count(countRoot));
		countQuery.where(buildPredicates(cb, countRoot, criteria).toArray(Predicate[]::new));
		long totalElements = entityManager.createQuery(countQuery).getSingleResult();

		int totalPages = totalElements == 0
				? 0
				: (int) Math.ceil((double) totalElements / pageRequest.size());

		return new PagedResult<>(items, pageRequest.page(), pageRequest.size(), totalElements, totalPages);
	}

	private List<Predicate> buildPredicates(
			CriteriaBuilder cb,
			Root<AdminActionLogEntity> root,
			AdminActionLogSearchCriteria criteria
	) {
		List<Predicate> predicates = new ArrayList<>();

		if (criteria.adminId() != null) {
			predicates.add(cb.equal(root.get("adminId"), criteria.adminId()));
		}
		if (criteria.actionType() != null) {
			predicates.add(cb.equal(root.get("actionType"), parseActionType(criteria.actionType())));
		}
		if (criteria.targetType() != null) {
			predicates.add(cb.equal(root.get("targetType"), criteria.targetType()));
		}
		if (criteria.targetId() != null) {
			predicates.add(cb.equal(root.get("targetId"), criteria.targetId()));
		}
		if (criteria.from() != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), criteria.from()));
		}
		if (criteria.to() != null) {
			predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), criteria.to()));
		}
		if (criteria.status() != null) {
			predicates.add(cb.like(
					root.get("responsePayload"),
					"%\"status\":\"" + criteria.status().name() + "\"%"
			));
		}

		return predicates;
	}

	private AdminActionType parseActionType(String actionType) {
		try {
			return AdminActionType.valueOf(actionType);
		} catch (IllegalArgumentException ex) {
			throw new AppException(
					ErrorCode.VALIDATION_ERROR,
					"Unknown action type: " + actionType,
					"action",
					"invalid value"
			);
		}
	}
}
