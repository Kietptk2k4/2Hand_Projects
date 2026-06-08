package com.twohands.admin_service.infrastructure.persistence.config;

import com.twohands.admin_service.domain.common.PageRequest;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.config.SystemConfigSearchCriteria;
import com.twohands.admin_service.infrastructure.persistence.jpa.entity.SystemConfigEntity;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.SystemConfigValueType;
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
public class SystemConfigSearchRepository {

	private final EntityManager entityManager;

	public SystemConfigSearchRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public PagedResult<SystemConfigEntity> search(SystemConfigSearchCriteria criteria, PageRequest pageRequest) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();

		CriteriaQuery<SystemConfigEntity> query = cb.createQuery(SystemConfigEntity.class);
		Root<SystemConfigEntity> root = query.from(SystemConfigEntity.class);
		List<Predicate> predicates = buildPredicates(cb, root, criteria);
		query.where(predicates.toArray(Predicate[]::new));
		query.orderBy(cb.asc(root.get("configKey")));

		TypedQuery<SystemConfigEntity> typedQuery = entityManager.createQuery(query);
		typedQuery.setFirstResult((pageRequest.page() - 1) * pageRequest.size());
		typedQuery.setMaxResults(pageRequest.size());
		List<SystemConfigEntity> items = typedQuery.getResultList();

		CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
		Root<SystemConfigEntity> countRoot = countQuery.from(SystemConfigEntity.class);
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
			Root<SystemConfigEntity> root,
			SystemConfigSearchCriteria criteria
	) {
		List<Predicate> predicates = new ArrayList<>();

		if (criteria.query() != null && !criteria.query().isBlank()) {
			String pattern = "%" + criteria.query().trim().toLowerCase() + "%";
			predicates.add(cb.or(
					cb.like(cb.lower(root.get("configKey")), pattern),
					cb.like(cb.lower(root.get("description")), pattern)
			));
		}
		if (criteria.valueType() != null) {
			predicates.add(cb.equal(
					root.get("valueType"),
					SystemConfigValueType.valueOf(criteria.valueType().name())
			));
		}
		if (criteria.active() != null) {
			predicates.add(cb.equal(root.get("active"), criteria.active()));
		}

		return predicates;
	}
}
