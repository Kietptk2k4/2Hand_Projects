package com.twohands.admin_service.infrastructure.persistence.announcement;

import com.twohands.admin_service.domain.announcement.SystemAnnouncementSearchCriteria;
import com.twohands.admin_service.domain.common.PageRequest;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.infrastructure.persistence.jpa.entity.SystemAnnouncementEntity;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AnnouncementSeverity;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AnnouncementStatus;
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
public class SystemAnnouncementSearchRepository {

	private final EntityManager entityManager;

	public SystemAnnouncementSearchRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public PagedResult<SystemAnnouncementEntity> search(
			SystemAnnouncementSearchCriteria criteria,
			PageRequest pageRequest
	) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();

		CriteriaQuery<SystemAnnouncementEntity> query = cb.createQuery(SystemAnnouncementEntity.class);
		Root<SystemAnnouncementEntity> root = query.from(SystemAnnouncementEntity.class);
		List<Predicate> predicates = buildPredicates(cb, root, criteria);
		query.where(predicates.toArray(Predicate[]::new));
		query.orderBy(cb.desc(root.get("createdAt")));

		TypedQuery<SystemAnnouncementEntity> typedQuery = entityManager.createQuery(query);
		typedQuery.setFirstResult((pageRequest.page() - 1) * pageRequest.size());
		typedQuery.setMaxResults(pageRequest.size());
		List<SystemAnnouncementEntity> items = typedQuery.getResultList();

		CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
		Root<SystemAnnouncementEntity> countRoot = countQuery.from(SystemAnnouncementEntity.class);
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
			Root<SystemAnnouncementEntity> root,
			SystemAnnouncementSearchCriteria criteria
	) {
		List<Predicate> predicates = new ArrayList<>();

		if (criteria.query() != null && !criteria.query().isBlank()) {
			String pattern = "%" + criteria.query().trim().toLowerCase() + "%";
			predicates.add(cb.or(
					cb.like(cb.lower(root.get("title")), pattern),
					cb.like(cb.lower(root.get("content")), pattern)
			));
		}
		if (criteria.status() != null) {
			predicates.add(cb.equal(
					root.get("status"),
					AnnouncementStatus.valueOf(criteria.status().name())
			));
		}
		if (criteria.severity() != null) {
			predicates.add(cb.equal(
					root.get("severity"),
					AnnouncementSeverity.valueOf(criteria.severity().name())
			));
		}

		return predicates;
	}
}
